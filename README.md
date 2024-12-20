# GenAI Foundations for Java Developers - Task-1
This the implemented practical task, addressing next requirements:
- Building the first application with Semantic Kernel in Java.
- Using the Semantic Kernel API to generate text based on the input text.

## Notes and findings
Set Up Project Environment --> Done
Obtain DIAL Key  --> Done)
Configuring the Application  --> Done
Implement Application Logic  --> Done
Final Integration and Submission  --> Done
Free Practice: Explore and Innovate : I've played with multiple configurations, leaving the last one in this code to limit the context
- Used bookPlugin to avoid model hallucination and reduce the scope.
- When asking for others topic then a message look at the attached images

First approach was to use the Semantic Kernel API to generate text based on the input text. It started answering any questions, 
but then I've decided to play with the configurations and see how the model behaves. I've tried to use the bookPlugin to avoid model hallucination and reduce the scope. 
Also I've changed the prompt to avoid answering for other topics, and limit it to Latin American history.

I added a application.properties file to store the API key, endpoint and model name. This way, it's easier to change the configuration without changing the code.


Task4:
Create series of custom plugins the existing system.
This function should be called by a model based on user request, it can return some data or make actions inside the application.
As ideas for plugins you can use:
Age calculator, Weather forecast, Currency converter, Turn of the lamp (just change the flag of some boolean variable),
and so on, in general, any plugin that can do some calculations or provide some information outside model knowledge


Task5:
Qdrant is a vector database that allows you to store and search for vectors.
Qdrant installation and configuration:
https://qdrant.tech/documentation/guides/installation/
https://qdrant.tech/documentation/interfaces/#grpc-interface
1. Pull docker image
  - docker pull qdrant/qdrant
  - Create a directory for storing the database called qdrant_storage 
  - From parent folder run:
    - docker run -p 6333:6333 -p 6334:6334 -v /qdrant_storage:/qdrant/storage:z qdrant/qdrant  
      - If you decide to use gRPC, you must expose 6334 the port when starting Qdrant.

2. Open dashboard http://localhost:6333/dashboard#/collections