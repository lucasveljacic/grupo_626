# Activity Recognition APP

El presente repo copntiene el codigo fuente de un servicio web, scripts python y una mobile application que son parte de un sistema cuyo proposito principal es el de reconocer la actividad de los usuarios utilizando Machine Learning.

## Arquitectura


### Detalle de modulos

#### SSO
Single Sign On provisto por UNLaM. Se compone de un API REST con los servicios necesarios para registrar y loguear un usuario.

#### Event Tracker
Servicio provisto por UNLaM. Se compone de un API REST con los servicios necesarios para informar evento de la APP.

#### Model Service
Servicio que expone un API REST que permite crear, entrenar, resetear (usado para reentrenar) y eliminar modelos asi como tambien expone un endpoint para realizar las predicciones.

#### Scripts Python
Utilizados para entrenar el modelo. Reciben un path con el path donde debe ir a buscar los datos de los sensores recolectados por la APP android y otro path donde debe guardar el modelo ya entrenado que sera usado en la fase de prediccion.
Este script es invocado por el Model Service cuando el modelo ha finalizado su fase de recoleccion de datos y debe conenzar el training.

#### Tensorflow Serving https://www.tensorflow.org/tfx/guide/serving
Este servicio es utilizado para el servido del modelo. Expone un API REST para realizar las predicciones. Solo se le debe proporcionar el path donde se encuentran los modelos a servir. Admite cambios y re-deploys de los modelos sin requerir reinicio del servicio lo cual lo hizo adecuado para el proyecto. 


### Modo de uso de la APP

#### Login / Registro
No mucho que aclarar, los pasos normales de una autenticacion basica.

#### Menu Principal
El menu principal muestra los siguientes botones:
* Logout. Realiza el logout de la APP. 
* Resetear el Modelo. Borra el modelo actual y lo crea nuevamente. 
* Captura usuario 1. Ingresa a la seccion de captura de datos de sensor para el usuario 1. Envia datos de los sensores al Model Server. El sensor listener empaqueta y promedia generando paquetes de 50 mediciones de 4 (3 coordenadas cada sensor, hacen 12 valores por medicion) sensores cada segundo. Enviando paquetes de 50x12 valores.   
* Captura usuario 2. Idem para el usuario 2.
* Entrenar el modelo. Envia un mensaje al Model Service para que realice la invocacion al Python script train.py y aguarda a su finalizacion. El modelo suele entrenar en ~1 minuto, pero el reload del modelo en el Tensorflow Service suele demorar hasta un minuto mas. Con lo cual este proceso demora unos 2 minutos aproximadamente.
* Prediccion de usuario. En esta seccion el usuario podra realizar acciones y la app indicara su prediccion sobre quien de los dos usuario, el 1 o el 2, es el que la realizo. Para esto, envia peticiones al Model Service que a su vez las delega en el Tensorflow Service. La captura de datos de los sensores se realiza de manera similar a la de la fase de captua, enviando paquetes de 50x12 una vez cada segundo.


### Consideraciones sobre el modelo
El modelo usado es una red neuronal de tipo RNN (Recursive Neural Network). Concretamente una red con una capa de LSTM (Long Short Term Memory) a la que se le entrena con inputs o tensores de (1, 50, 12), es decir, con una sequencia temporal de mediciones correspondiente a 1 segundo de actividad de cada usuario. El target que se le indica al modelo para cada entrada es 0 o 1 indicando Usuario 1 o 2 respectivamente. 
El modelo y las pruebas realizadas se puede ver aqui: [TBD]



### Conclusiones
* Los resultados obtenidos son prometedores, el modelo logra predecir con bastante certeza los usuarios.
* Algo que es destacable es que no es requerido que el Usuario 1 sea una persona y Usuario 2 sea otra, en realidad, el concepto es reconocer patrones de actividad de sensores, asi que puede usarse para relaizar HAR (Human Activity Recognition). Por ejemplo, se puede caminar durante todo la camputa de Usuario 1, y quedarse quieto o correr durante la del Usuario 2. Luego en fase de inferencia el modelo sera capaz de reconocer la actividad que esta haciendo el usuario. 
* Siempre la clasificacion es binaria pero se podria extender a una clasificaciones de N clases.




























