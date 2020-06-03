#!/usr/bin/env python

import argparse
import pandas as pd
import numpy as np
from numpy import array
from numpy.random import seed
import tensorflow as tf
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import LSTM, Dense
from tensorflow.keras.callbacks import EarlyStopping, ModelCheckpoint
import time
import os
from sklearn.utils import shuffle
from shutil import rmtree
import warnings

warnings.filterwarnings("ignore")
# make the post request 
start_time = time.time()

parser = argparse.ArgumentParser(description='Train model for activity recognition')
parser.add_argument("--basepath", required=True, type=str, help="This is the base path for input and output data")

args = parser.parse_args()

base_path = args.basepath
output_path = os.path.join(base_path, "model")
input_path = os.path.join(base_path, "train")
checkpoint_file = os.path.join(base_path, "best_model.h5")
success_file = os.path.join(input_path, "SUCCESS")

if not os.path.exists(input_path):
    exit(1)
    
if not os.path.exists(output_path):
    os.makedirs(output_path)
else:
    rmtree(output_path)

if os.path.exists(success_file):
    os.remove(success_file)
    
seed(1652)
tf.set_random_seed(432)

n_steps=50
n_features=12

raw1_df = pd.read_csv("{}/measures_1.csv".format(input_path), 
                      header=None, 
                      names=("t", "s1", "v11", "v12", "v13", "s2", "v21", "v22", "v23", "s3", "v31", "v32", "v33", "s4", "v41", "v42", "v43"))

raw2_df = pd.read_csv("{}/measures_2.csv".format(input_path), 
                      header=None, 
                      names=("t", "s1", "v11", "v12", "v13", "s2", "v21", "v22", "v23", "s3", "v31", "v32", "v33", "s4", "v41", "v42", "v43"))


df1 = raw1_df[["v11", "v12", "v13", "v21", "v22", "v23", "v31", "v32", "v33", "v41", "v42", "v43"]]
df2 = raw2_df[["v11", "v12", "v13", "v21", "v22", "v23", "v31", "v32", "v33", "v41", "v42", "v43"]]

df1.loc[:, 'target'] = 0
df2.loc[:, 'target'] = 1

def split_sequences(sequences, n_steps):
    X, y = list(), list()
    for i in range(len(sequences)):
        # find the end of this pattern
        end_ix = i + n_steps
        # check if we are beyond the dataset
        if end_ix > len(sequences):
            break
        # gather input and output parts of the pattern
        seq_x, seq_y = sequences[i:end_ix, :-1], sequences[end_ix-1, -1]
        X.append(seq_x)
        y.append(seq_y)
    return array(X), array(y)

df1arr = df1.to_numpy()
df1arr_train = df1arr[int(df1arr.shape[0]*0.25):]
df1arr_test  = df1arr[:int(df1arr.shape[0]*0.25)]

df2arr = df2.to_numpy()
df2arr_train = df2arr[int(df2arr.shape[0]*0.25):]
df2arr_test  = df2arr[:int(df2arr.shape[0]*0.25)]

X1_train, y1_train = split_sequences(df1arr_train, n_steps)
X2_train, y2_train = split_sequences(df2arr_train, n_steps)

y1_train = y1_train.reshape(y1_train.shape[0], 1)
y2_train = y2_train.reshape(y2_train.shape[0], 1)

X1_test, y1_test = split_sequences(df1arr_test, n_steps)
X2_test, y2_test = split_sequences(df2arr_test, n_steps)

y1_test = y1_test.reshape(y1_test.shape[0], 1)
y2_test = y2_test.reshape(y2_test.shape[0], 1)

X_train = np.vstack((X1_train, X2_train))
y_train = np.vstack((y1_train, y2_train))

X_test = np.vstack((X1_test, X2_test))
y_test = np.vstack((y1_test, y2_test))

X_train, y_train = shuffle(X_train, y_train, random_state=0)


# define model
model = tf.keras.Sequential()
model.add(LSTM(200, activation='relu', input_shape=(n_steps, n_features)))
model.add(Dense(1, activation='sigmoid'))

model.compile(loss='binary_crossentropy', optimizer='adam', metrics=['accuracy'])

model.summary()

# fit model
# simple early stopping

es = EarlyStopping(monitor='val_loss', mode='min', verbose=1, patience=3, min_delta=1)
mc = ModelCheckpoint(checkpoint_file, monitor='val_loss', mode='min', verbose=1, save_best_only=True)

history = model.fit(
    X_train, y_train, 
    validation_data=(X_test, y_test),
    epochs=30, batch_size=64, callbacks=[es, mc])

model.load_weights(checkpoint_file)

scores = model.evaluate(X_test, y_test)
print("Accuracy: %.2f%%" % (scores[1]*100))

# saving model
tf.keras.experimental.export_saved_model(model, "{}/1".format(output_path))

# notifying success
with open(os.path.join(input_path, "SUCCESS"), 'w') as fp: 
    pass

elapsed_time = time.time() - start_time

print("\n\nTrain duration: {} seconds\n\n".format(elapsed_time))

exit(0)