package org.activityrecognition.measure;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

@Service
public class MeasureService {
    private Logger logger = Logger.getLogger(MeasureService.class.getName());
    public void append(String packet) throws IOException {
        logger.info(packet);

        File file = new File("/home/lucas/src/grupo_626/CODIGO/models/data/measures.csv");
        FileWriter fr = new FileWriter(file, true);
        fr.write(packet.trim()+"\n");
        fr.close();
    }
}
