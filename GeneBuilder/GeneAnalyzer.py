from __future__ import absolute_import, division, print_function

import os
import matplotlib.pyplot as plt

import tensorflow as tf
import tensorflow.contrib.eager as tfe

tf.enable_eager_execution()

print("TensorFlow version: {}".format(tf.VERSION))
print("Eager execution: {}".format(tf.executing_eagerly()))

train_dataset_fp = "D:\Dropbox\Public\GenomeOptimizer\GeneBuilder\TrainingData.txt"

print("Local copy of the dataset file: {}".format(train_dataset_fp))

def parse_csv(line):
	example_defaults = [[0], [0.], [0.], [0.]]  # sets field types
	parsed_line = tf.decode_csv(line, example_defaults)
	# First 4 fields are features, combine into single tensor
	feature = tf.reshape(parsed_line[1:4], shape=(3,))
	print("====================================================== A")
	# Last field is the label
	labels = tf.reshape(parsed_line[0:1], shape=())
	print("====================================================== B")
	return feature, labels

train_dataset = tf.data.TextLineDataset(train_dataset_fp)
train_dataset = train_dataset.skip(1)             # skip the first header row
train_dataset = train_dataset.map(parse_csv)      # parse each row
print("====================================================== C")
train_dataset = train_dataset.shuffle(buffer_size=100)  # randomize
print("====================================================== D")
train_dataset = train_dataset.batch(64)
print("====================================================== E")

# View a single example entry from a batch
features, label = tfe.Iterator(train_dataset).next()
print("====================================================== F")
print("example features:", features[0])
print("====================================================== G")
print("example label:", label[0])
print("====================================================== H")

model = tf.keras.Sequential([
  tf.keras.layers.Dense(3, activation="relu", input_shape=(3,)),  # input shape required
  tf.keras.layers.Dense(640, activation="relu"),
  tf.keras.layers.Dense(640, activation="relu"),
  tf.keras.layers.Dense(640, activation="relu"),
  tf.keras.layers.Dense(640, activation="relu"),
  tf.keras.layers.Dense(640, activation="relu"),
  tf.keras.layers.Dense(640, activation="relu"),
  tf.keras.layers.Dense(10)
])
print("====================================================== I")

def loss(model, x, y):
	y_ = model(x)
	return tf.losses.sparse_softmax_cross_entropy(labels=y, logits=y_)
print("====================================================== J")


def grad(model, inputs, targets):
	with tfe.GradientTape() as tape:
		loss_value = loss(model, inputs, targets)
	return tape.gradient(loss_value, model.variables)
print("====================================================== K")

optimizer = tf.train.GradientDescentOptimizer(learning_rate=0.00001)
print("====================================================== L")

## Note: Rerunning this cell uses the same model variables

# keep results for plotting
train_loss_results = []
train_accuracy_results = []

num_epochs = 200

for epoch in range(num_epochs):
	epoch_loss_avg = tfe.metrics.Mean()
	epoch_accuracy = tfe.metrics.Accuracy()

	# Training loop - using batches of 64
	for x, y in tfe.Iterator(train_dataset):
		# Optimize the model
		grads = grad(model, x, y)
		optimizer.apply_gradients(zip(grads, model.variables), global_step=tf.train.get_or_create_global_step())

		# Track progress
		epoch_loss_avg(loss(model, x, y))  # add current batch loss
		# compare predicted label to actual label
		epoch_accuracy(tf.argmax(model(x), axis=1, output_type=tf.int32), y)

		# end epoch
		train_loss_results.append(epoch_loss_avg.result())
		train_accuracy_results.append(epoch_accuracy.result())
  
	print("Epoch {:03d}: Loss: {:.3f}, Accuracy: {:.3%}".format(epoch, epoch_loss_avg.result(), epoch_accuracy.result()))

test_fp = "D:\Dropbox\Public\GenomeOptimizer\GeneBuilder\TestData.txt"
test_dataset = tf.data.TextLineDataset(test_fp)
test_dataset = test_dataset.skip(1)             # skip header row
test_dataset = test_dataset.map(parse_csv)      # parse each row with the function created earlier
test_dataset = test_dataset.shuffle(1000)       # randomize
test_dataset = test_dataset.batch(128)           # use the same batch size as the training set
test_accuracy = tfe.metrics.Accuracy()
for (x, y) in tfe.Iterator(test_dataset):
  prediction = tf.argmax(model(x), axis=1, output_type=tf.int32)
  test_accuracy(prediction, y)

print("Test set accuracy: {:.3%}".format(test_accuracy.result()))