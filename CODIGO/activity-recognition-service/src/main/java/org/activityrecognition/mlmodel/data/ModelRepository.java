package org.activityrecognition.mlmodel.data;

import org.activityrecognition.mlmodel.model.Model;

public interface ModelRepository {
    Model save(Model model);

    Model findById(String id);
}
