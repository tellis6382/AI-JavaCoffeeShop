package org.workshop.coffee.export;

import org.workshop.coffee.domain.Order;
import org.workshop.coffee.domain.Person;
import org.springframework.http.MediaType;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class YamlImportExport {


    public static final MediaType MEDIA_TYPE_YAML = MediaType.valueOf("text/yaml");

    public static String exportOrders(List<Order> orders) {
        var exportOrders = orders.stream()
                .map(ExportOrder::fromOrder)
                .collect(Collectors.toList());

        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        return yaml.dump(exportOrders);
    }


    public static List<Order> importOrders(InputStream inputStream, Person person) {
        // Safe deserialization: restrict to known types only to prevent
        // SnakeYAML gadget-chain RCE via arbitrary class instantiation.
        Constructor constructor = new Constructor(ExportOrder.class);
        TypeDescription exportOrderDesc = new TypeDescription(ExportOrder.class);
        exportOrderDesc.addPropertyParameters("orderLines", ExportOrderLine.class);
        constructor.addTypeDescription(exportOrderDesc);

        Yaml yaml = new Yaml(constructor);
        Iterable<Object> documents = yaml.loadAll(inputStream);

        return java.util.stream.StreamSupport.stream(documents.spliterator(), false)
                .flatMap(doc -> {
                    if (doc instanceof List) {
                        return ((List<?>) doc).stream();
                    }
                    return java.util.stream.Stream.of(doc);
                })
                .filter(ExportOrder.class::isInstance)
                .map(ExportOrder.class::cast)
                .map(eo -> ExportOrderConvertor.createOrders(eo, person))
                .collect(Collectors.toList());
    }

}
