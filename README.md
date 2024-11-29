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