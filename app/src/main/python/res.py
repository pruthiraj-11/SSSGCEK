import tensorflow as tf
import numpy as np
import pandas as pd
import json
import nltk
import string
import random
from tensorflow.keras.preprocessing.text import Tokenizer
from tensorflow.keras.layers import Input, Embedding, LSTM, Dense, GlobalMaxPooling1D, Flatten
from tensorflow.keras.preprocessing.text import Tokenizer
from tensorflow.keras.models import Model
from tensorflow.keras.preprocessing.sequence import pad_sequences
from sklearn.preprocessing import LabelEncoder
import matplotlib.pyplot as plt
from urllib.request import urlopen
import ssl


def backend(txt):
    url = "https://jsonkeeper.com/b/2MQ1"
    context = ssl._create_unverified_context()b
    response = urlopen(url, context=context)
    data1 = json.load(response)
    tags = []
    inputs = []
    responses = {}
    for intent in data1['intents']:
        responses[intent['tag']] = intent['responses']
        for lines in intent['patterns']:
            inputs.append(lines)
            tags.append(intent['tag'])

    data = pd.DataFrame({"inputs": inputs, "tags": tags})
    data['inputs'] = data['inputs'].apply(
        lambda wrd: [ltrs.lower() for ltrs in wrd if ltrs not in string.punctuation])
    data['inputs'] = data['inputs'].apply(lambda wrd: ''.join(wrd))
    tokenizer = Tokenizer(num_words=2000)
    tokenizer.fit_on_texts(data['inputs'])
    train = tokenizer.texts_to_sequences(data['inputs'])
    x_train = pad_sequences(train)
    le = LabelEncoder()
    y_train = le.fit_transform(data['tags'])
    input_shape = x_train.shape[1]
    vocabulary = len(tokenizer.word_index)
    output_length = le.classes_.shape[0]
    i = Input(shape=(input_shape,))
    x = Embedding(vocabulary + 1, 10)(i)
    x = LSTM(10, return_sequences=True)(x)
    x = Flatten()(x)
    x = Dense(output_length, activation="softmax")(x)
    model = Model(i, x)
    model.compile(loss="sparse_categorical_crossentropy", optimizer='adam', metrics=['accuracy'])
    # train=model.fit(x_train,y_train,epochs=200)
    texts_p = []
    prediction_input = txt
    prediction_input = [letters.lower() for letters in prediction_input if
                        letters not in string.punctuation]
    prediction_input = ''.join(prediction_input)
    texts_p.append(prediction_input)
    prediction_input = tokenizer.texts_to_sequences(texts_p)
    prediction_input = np.array(prediction_input).reshape(-1)
    prediction_input = pad_sequences([prediction_input], input_shape)
    output = model.predict(prediction_input)
    output = output.argmax()
    response_tag = le.inverse_transform([output])[0]
    return random.choice(responses[response_tag])
