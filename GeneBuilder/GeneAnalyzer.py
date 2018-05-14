from __future__ import absolute_import, division, print_function

import os
import matplotlib.pyplot as plt
import math

import tensorflow as tf
import tensorflow.contrib.eager as tfe
import numpy as np

tf.enable_eager_execution()

print("TensorFlow version: {}".format(tf.VERSION))
print("Eager execution: {}".format(tf.executing_eagerly()))

train_dataset_fp = "TrainingData.csv"

print("Local copy of the dataset file: {}".format(train_dataset_fp))

# Hard-Coded
basesPerAminoAcid = 3
statCount = 3
batchSize = 10
numEpochs = 200
alpha = 0.00001

# Automatically Detected
basePairs = 0
genomeLength = 0
numCreatures = 0
traitAvg = 0

with open(train_dataset_fp) as f:
	print("Reading Parameters from " + train_dataset_fp)
	line = f.readline()
	for item in line.split(','):
		paramStart = item.find('=') + 1
		if item.find('CreatureCount') == 0:
			paramValue = int(item[paramStart:])
			print("Set CreatureCount to " + str(paramValue))
			numCreatures = paramValue
		elif item.find('TraitCount') == 0:
			paramValue = int(item[paramStart:])
			print("Set TraitCount to " + str(paramValue))
			statCount = paramValue
		elif item.find('TraitAvg') == 0:
			paramValue = float(item[paramStart:])
			print("Set TraitAvg to " + str(paramValue))
			traitAvg = paramValue
		elif item.find('BasePairCount') == 0:
			paramValue = int(item[paramStart:])
			print("Set BasePairCount to " + str(paramValue))
			basePairs = paramValue
		else:
			print("Unrecognized Parameter: " + item)
	print("Parameter Read Complete")
genomeLength = basePairs/basesPerAminoAcid

print("Loading Database...")
train_dataset = tf.data.TextLineDataset(train_dataset_fp)
genomeReaderList = ([[0.]] * statCount) + ([[0.]] * basePairs)
print("Genome Reader list: ", genomeReaderList)
def parse_csv(line):
	parsed_line = tf.decode_csv(line, genomeReaderList)
	# First 4 fields are features, combine into single tensor
	features = tf.reshape(parsed_line[statCount:(statCount+basePairs)], shape=(basePairs,))
	# Last field is the label
	labels = tf.reshape(parsed_line[0:statCount], shape=(statCount,))
	return features, labels

train_dataset = train_dataset.skip(1)             # skip the first header row
print("Mapping Database...")
train_dataset = train_dataset.map(parse_csv)      # parse each row
print("Shuffling Database...")
train_dataset = train_dataset.shuffle(buffer_size=100)  # randomize
print("Batching Database...")
train_dataset = train_dataset.batch(batchSize)

# View a single example entry from a batch
features, label = tfe.Iterator(train_dataset).next()
print("example features:", features[0])
print("example label:", label[0])

print("Generating Network...")
neuralNetwork = tf.keras.Sequential([
  tf.keras.layers.Dense(basePairs, activation="relu", input_shape=(basePairs,)),  # input shape required
  tf.keras.layers.Dense(basePairs*3, activation="relu"),
  tf.keras.layers.Dense(basePairs*3, activation="relu"),
  tf.keras.layers.Dense(statCount)
])

def loss(prediction, groundTruth):
	totalLoss = tf.losses.mean_squared_error(labels=groundTruth, predictions=prediction)
	return totalLoss


def grad(neuralNetwork, inputs, groundTruth):
	with tfe.GradientTape() as tape:
		prediction = neuralNetwork(inputs)
		loss_value = tf.losses.mean_squared_error(labels=groundTruth, predictions=prediction)
	return tape.gradient(loss_value, neuralNetwork.variables)

optimizer = tf.train.GradientDescentOptimizer(learning_rate=alpha)

## Note: Rerunning this cell uses the same neuralNetwork variables

# keep results for plotting
train_loss_results = []

print("=====================TRAIN========================")
for epoch in range(numEpochs):
	epoch_loss_avg = tfe.metrics.Mean()
	epoch_accu_avg = tfe.metrics.Mean()

	# Training loop - using batches
	for inputs, groundTruth in tfe.Iterator(train_dataset):
		
		# Optimize the neuralNetwork
		grads = grad(neuralNetwork, inputs, groundTruth)
		optimizer.apply_gradients(zip(grads, neuralNetwork.variables), global_step=tf.train.get_or_create_global_step())

		# Calculate Loss
		prediction = neuralNetwork(inputs)
		epoch_loss_avg(loss(prediction, groundTruth))
		train_loss_results.append(epoch_loss_avg.result())
	
	epochLossResult = epoch_loss_avg.result()
	baselineString = "E:{:03d} Loss: {:.3f} (Per Trait: {:.3f})"
	print(baselineString.format(epoch, epochLossResult, math.sqrt(epochLossResult/statCount)))


## Test
print("=====================TEST========================")
test_fp = "D:\Dropbox\Public\GenomeOptimizer\GeneBuilder\TestData.csv"
test_dataset = tf.data.TextLineDataset(test_fp)
test_dataset = test_dataset.skip(1)             # skip header row
test_dataset = test_dataset.map(parse_csv)      # parse each row with the function created earlier
test_dataset = test_dataset.shuffle(100)       # randomize
test_dataset = test_dataset.batch(batchSize)
tests = 0
testLoss = 0
for (inputs, groundTruth) in tfe.Iterator(test_dataset):
	prediction = neuralNetwork(inputs)
	individualLoss = loss(prediction, groundTruth)
	print("Test[" + str(tests) + "]: Loss=" + str(individualLoss))
	print("Pred: " + str(prediction[0, ::]))
	print("True: " + str(groundTruth[0, ::]))
	testLoss += individualLoss
	tests += 1
	print("=================================================")
averageTestLoss = testLoss/tests
print("Average Test Set Loss: {:.3f} (Per Trait: {:.3f})".format(averageTestLoss, math.sqrt(averageTestLoss/statCount)))