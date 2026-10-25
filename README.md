
#  Asistente de Estudio con IA Local

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-build-C71A36?logo=apachemaven&logoColor=white)
![Ollama](https://img.shields.io/badge/Ollama-modelo%20local-black)
![Estado](https://img.shields.io/badge/estado-en%20desarrollo-yellow)

Programa en Java que recibe una pregunta y la responde usando un modelo de inteligencia artificial que **corre 100 % en tu computadora**: sin internet, sin API keys y sin costo.

Proyecto personal creado para practicar Java, consumo de APIs y manejo de dependencias con Maven.

##  Demo

![Demo del programa](docs/captura.png)

Ejemplo de uso:

```
Escribe tu pregunta: ¿Qué es una cola en estructuras de datos?
Pensando...

Una cola es una estructura de datos que sigue el principio FIFO
(el primero en entrar es el primero en salir)...
```

## Características

- Pregunta por consola y respuesta generada por un modelo de lenguaje local.
- Comunicación con la API de Ollama mediante el cliente HTTP de Java.
- Manejo de errores: avisa si Ollama no está abierto o si el modelo no existe.
- Sin claves ni datos enviados a servidores externos.

## Tecnologías

| Tecnología | Para qué se usa |
|---|---|
| Java 17 | Lenguaje principal |
| Maven | Gestión del proyecto y dependencias |
| Gson | Armar y leer JSON |
| `java.net.http` | Peticiones HTTP a la API |
| Ollama + llama3.2:1b | Modelo de IA local |

##  Requisitos

- [JDK 17 o superior](https://adoptium.net)
- [Maven](https://maven.apache.org)
- [Ollama](https://ollama.com) instalado y abierto
- Unos 2 GB libres de disco para el modelo

##  Instalación y uso

1. Clona el repositorio:
   ```bash
   git clone https://github.com/TU_USUARIO/asistente-estudio-ia.git
   cd asistente-estudio-ia
   ```

2. Descarga el modelo (solo la primera vez):
   ```bash
   ollama pull llama3.2:1b
   ```

3. Asegúrate de que Ollama esté corriendo (abre la app, o ejecuta `ollama serve` en otra terminal).

4. Ejecuta el programa:
   ```bash
   mvn clean compile exec:java -Dexec.mainClass=Main
   ```

5. Escribe tu pregunta y espera la respuesta. La primera vez puede tardar unos segundos mientras el modelo se carga en memoria.

##  Cómo funciona

1. El programa lee tu pregunta desde la consola.
2. Construye un JSON con el modelo y el mensaje usando Gson.
3. Lo envía por HTTP a `http://localhost:11434/api/chat` (servidor local de Ollama).
4. Lee el JSON de respuesta y muestra el texto generado.

##  Estructura del proyecto

```
asistente-estudio-ia/
├── docs/
│   └── captura.png
├── src/main/java/
│   └── Main.java
├── pom.xml
└── README.md
```

##  Próximos pasos

- [x] Enviar una pregunta a un modelo local y mostrar la respuesta
- [ ] Leer apuntes desde un archivo `.txt` y usarlos como contexto
- [ ] Generar preguntas de práctica a partir de los apuntes
- [ ] Separar el código en clases (cliente, lector de apuntes, quiz)
- [ ] Agregar una interfaz gráfica

## Lo que aprendí

- Consumir una API REST con el cliente HTTP de Java.
- Organizar un proyecto con Maven y agregar dependencias.
- Trabajar con JSON usando Gson.
- Usar Git y GitHub para versionar un proyecto.
- Depurar errores reales de configuración, rutas y entorno.

##  Autor

**Israel Santos**, estudiante de Ingeniería en Informática en Instituto Tecnológico de Durango (ITD).

- LinkedIn: [Israel Santos](www.linkedin.com/in/israelsantosalvarado)
- GitHub: [IsraelSantos1445](https://www.linkedin.com/in/israelsantosalvarado)
