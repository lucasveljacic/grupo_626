package org.activityrecognition.mlmodel.data;

import org.activityrecognition.mlmodel.model.Model;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class ModelInMemoryRepository implements ModelRepository {

    private Map<String, Model> models = new HashMap<>();

    @Override
    public Model save(Model model) {
        if (!models.containsKey(model.getId())) {
            models.put(model.getId(), model);
        }
        return models.get(model.getId());
    }

    @Override
    public Model findById(String id) {
        return models.getOrDefault(id, null);
    }
}
