const { Client, logger, Variables } = require("camunda-external-task-client-js");

// configuration for the Client:
//  - 'baseUrl': url to the Workflow Engine
//  - 'logger': utility to automatically log important events
const config = { baseUrl: "http://localhost:8080/engine-rest", 
    use: logger,
	workerId: 'MyJavaScriptWorker',
	lockDuration: 15000,
    maxTasks: 1};
	
// create a Client instance with custom configuration
const client = new Client(config);

// susbscribe to the topic: 'creditScoreChecker'
client.subscribe("chargeCredit", async function({task, taskService}) {
	try{
		console.log("Charging Credit Card...");
		await taskService.complete(task);
	}catch(err){
		console.log(err);
	}
});