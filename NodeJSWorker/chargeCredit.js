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
		const amount = task.variables.get("amount");
		const remaining = task.variables.get("remaining");
		const shouldBpmnError = false;
		//task.variables.get("shouldBpmnError");
		console.log("Charging Credit Card...");
		
		const processVariables = new Variables();
		processVariables.set("remaining", 0.0);
		
	if(shouldBpmnError) {
		console.log("BPMN Error will be produced!");
		await taskService.handleBpmnError(task, "chargeFailed", "Charging failed, because of technical difficulties");
	}else {
		await taskService.complete(task, processVariables);
	}
	}catch(err){
		console.log(err);
	}
});