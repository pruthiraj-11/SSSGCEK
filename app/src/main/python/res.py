import tensorflow as tf
import numpy as np
import pandas as pd
import json
import nltk
import string
import random
from tensorflow import keras
from tensorflow.keras.preprocessing.text import Tokenizer
from tensorflow.keras.layers import Input, Embedding, LSTM, Dense, GlobalMaxPooling1D, Flatten
from tensorflow.keras.preprocessing.text import Tokenizer
from tensorflow.keras.models import Model
from tensorflow.keras.preprocessing.sequence import pad_sequences
from sklearn.preprocessing import LabelEncoder
import matplotlib.pyplot as plt
from urllib.request import urlopen
import ssl
import joblib
import pickle

def backend(txt):
    url = "https://jsonkeeper.com/b/XE5X"
    context = ssl._create_unverified_context()
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
    input_shape = x_train.shape[1]
    le.fit_transform(data['tags'])
    output_length = le.classes_.shape[0]
    url1="https://firebasestorage.googleapis.com/v0/b/sspgcek.appspot.com/o/model.pkl?alt=media&token=dfb10431-1672-46d3-93ea-74183f68b627"
    response1 = urlopen(url1, context=context)
    model = pickle.load(response1)
    texts_p = []
    prediction_input = str(txt)
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
