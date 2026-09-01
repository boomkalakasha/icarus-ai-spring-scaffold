package io.github.boomkalakasha.icarus.scaffold.core.template;

import java.util.List;

/** Test-only ServiceLoader provider proving that a pack can own classpath resources. */
public final class ClasspathTestTemplatePack implements TemplatePack {

    @Override
    public String id() {
        return "classpath-test";
    }

    @Override
    public List<TemplateDefinition> templates() {
        return List.of(new TemplateDefinition(
                "classpath-test/classpath-pack.txt.ftl",
                "classpath-pack.txt"));
    }
}
