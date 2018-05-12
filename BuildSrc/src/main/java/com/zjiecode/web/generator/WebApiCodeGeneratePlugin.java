package com.zjiecode.web.generator;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class WebApiCodeGeneratePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        ConfigExtension configExtension = project.getExtensions().create("config", ConfigExtension.class);
        GenerateTask generateTask = project.getTasks().create(
                "generateCode", GenerateTask.class,
                task -> task.setGroup("generate code"));
        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                generateTask.setConfig(configExtension);
            }
        });

    }

}
