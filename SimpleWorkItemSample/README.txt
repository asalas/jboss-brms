Para agregar un service task customizado al diseñador de jBPM es necesario realizar lo siguiente:

1) dentro del directorio src/main/resources se debe crear el folder icons y agregar las imagenes en formato png que se usarán como iconos de la tarea.

2) dentro del directorio src/main/resources se debe crear el folder META/INF y agregar los siguientes archivos:
drool.rulebase.conf y debe tener como contenido la definición del work item handler que se va a agregar. Por ejemplo:

dools.workDefinitions = workItemDefinitions.conf

También se debe agregar el archivo workItemDefinitions.conf, que es utilizado dentro del archivo de configuración del drools anterior.
Solo que en este archiv se debe agregar la definición del work item en formato JSON como por ejemplo:

import org.drools.process.core.datatype.impl.type.StringDataType;
[
  [
    "name" : "multiplier_process",
    "parameters" : [
      "description" : new StringDataType(),
    ],
    "displayName" : "Multiply",
    "icon" : "icons/multiplier_pocess_icon.png"
  ]
  
]


3) Una vez realizados los pasos anteriores se debe cerrar y volver a abrir el archivo bpmn y revisar que el nuevo service task se haya agregado correctamente.