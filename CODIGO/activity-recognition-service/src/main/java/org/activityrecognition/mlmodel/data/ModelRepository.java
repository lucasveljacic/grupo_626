package org.activityrecognition.mlmodel.data;

import org.activityrecognition.mlmodel.model.Model;

import java.util.List;

public interface ModelRepository {
    Model save(Model model);

    Model findById(String id);

    List<Model> findAll();
}
