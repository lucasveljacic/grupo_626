
## App user creation

    sudo chown activity-recognition-service:activity-recognition-service /opt/activity-recognition-service-0.0.1-SNAPSHOT.jar 
    sudo chown activity-recognition-service:activity-recognition-service /opt/activity-recognition-service-0.0.1-SNAPSHOT.jar 
    sudo chmod 500 activity-recognition-service-0.0.1-SNAPSHOT.jar
    sudo ln -s /opt/activity-recognition-service-0.0.1-SNAPSHOT.jar /opt/activity-recognition-service.jar

## Systemd daemon configuration

    # sudo vim /usr/lib/systemd/system/activity-recognition.service
    
    After=syslog.target
    
    [Service]
    WorkingDirectory=/opt/activity-recognition
    User=activity-recognition-service
    ExecStart=/bin/java -Xms128m -Xmx512m -jar activity-recognition-service.jar
    SuccessExitStatus=143
    Restart=always
    RestartSec=5
    
    [Install]
    WantedBy=multi-user.target

## reload conf
    
    sudo systemctl daemon-reload

## manage commands

    sudo systemctl enable activity-recognition.service
    sudo systemctl start activity-recognition.service
    sudo systemctl stop activity-recognition.service
    sudo systemctl status activity-recognition.service
    sudo systemctl restart activity-recognition.service


## logs

    sudo journalctl -u my-application.service -f

--------

# instalation of tensorflow-model-service
     
     echo "deb [arch=amd64] http://storage.googleapis.com/tensorflow-serving-apt stable tensorflow-model-server tensorflow-model-server-universal" | sudo tee /etc/apt/sources.list.d/tensorflow-serving.list && curl https://storage.googleapis.com/tensorflow-serving-apt/tensorflow-serving.release.pub.gpg | sudo apt-key add -
     sudo apt-get update
     sudo apt install tensorflow-model-server
     
     
    tensorflow_model_server --rest_api_port=9000 --model_config_file=/var/models/tensorflow-server-examples/models.config --model_config_file_poll_wait_seconds=30
