import random
import json
import pickle
import numpy as np
import nltk
from os.path import dirname, join
from keras.models import load_model
from nltk.stem import WordNetLemmatizer
from urllib.request import urlopen
import ssl

lemmatizer = WordNetLemmatizer()
url = "https://jsonkeeper.com/b/XE5X"
context = ssl._create_unverified_context()
response = urlopen(url,context=context)
intents=json.load(response)

# words = pickle.load(open('words.pkl', 'rb'))
url1="https://firebasestorage.googleapis.com/v0/b/sspgcek.appspot.com/o/words.pkl?alt=media&token=c70a05ce-60de-4fab-877e-b54045cacbbd7"
response1 = urlopen(url1, context=context)
words = pickle.load(response1)

# classes = pickle.load(open('classes.pkl', 'rb'))
url2="https://firebasestorage.googleapis.com/v0/b/sspgcek.appspot.com/o/classes.pkl?alt=media&token=4a1afe70-c790-4252-a715-9019c1e89c81"
response2 = urlopen(url2, context=context)
classes = pickle.load(response2)

filename = join(dirname(__file__), "chatbotmodel.h5")
model = load_model(filename)

def clean_up_sentences(sentence):
    sentence_words = nltk.word_tokenize(sentence)
    sentence_words = [lemmatizer.lemmatize(word)
                      for word in sentence_words]
    return sentence_words

def bagw(sentence):
    sentence_words = clean_up_sentences(sentence)
    bag = [0]*len(words)
    for w in sentence_words:
        for i, word in enumerate(words):
            if word == w:
                bag[i] = 1
    return np.array(bag)

def predict_class(sentence):
    bow = bagw(sentence)
    res = model.predict(np.array([bow]))[0]
    ERROR_THRESHOLD = 0.25
    results = [[i, r] for i, r in enumerate(res)
               if r > ERROR_THRESHOLD]
    results.sort(key=lambda x: x[1], reverse=True)
    return_list = []
    for r in results:
        return_list.append({'intent': classes[r[0]],
                            'probability': str(r[1])})
        return return_list

def get_response(intents_list, intents_json):
    tag = intents_list[0]['intent']
    list_of_intents = intents_json['intents']
    result = ""
    for i in list_of_intents:
        if i['tag'] == tag:
            result = random.choice(i['responses'])
            break
    return result

def backend(text):
    ints = predict_class(str(text))
    res = get_response(ints, intents)
    return res