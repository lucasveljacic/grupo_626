# Load libraries
import flask
import tensorflow as tf
import numpy as np

# instantiate flask
app = flask.Flask(__name__)

print(tf.keras.__version__)
print(tf.__version__)

global graph, model

graph = tf.get_default_graph()
model = tf.keras.models.load_model('/home/lucas/src/grupo_626/CODIGO/models/recognition.h5')


# define a predict function as an endpoint
@app.route("/predict", methods=["GET", "POST"])
def predict():
    data = {"success": False}

    params = flask.request.json
    if params is None:
        params = flask.request.args

    input_data = np.array(np.random.random_sample([1, 50, 12]), dtype=np.float32)
    with graph.as_default():
        pred = model.predict(input_data)[0][0]

        print(pred)

        data["prediction"] = str(pred)
        data["success"] = True

    # return a response in json format
    return flask.jsonify(data)


# start the flask app, allow remote connections
if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
