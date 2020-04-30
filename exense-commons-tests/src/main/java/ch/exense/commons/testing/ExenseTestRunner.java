package ch.exense.commons.testing;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import ch.exense.commons.testing.annotations.Parameters;
import ch.exense.commons.testing.annotations.PlanNames;
import step.core.artefacts.AbstractArtefact;
import step.core.artefacts.reports.ReportNodeStatus;
import step.core.execution.ExecutionContext;
import step.core.plans.Plan;
import step.core.plans.builder.PlanBuilder;
import step.core.plans.runner.PlanRunner;
import step.core.plans.runner.PlanRunnerResult;
import step.handlers.javahandler.Keyword;
import step.localrunner.LocalPlanRunner;
import step.planbuilder.BaseArtefacts;
import step.planbuilder.FunctionArtefacts;
import step.plans.nl.parser.PlanParser;

public class ExenseTestRunner extends ParentRunner<Plan> {
	
	private static String DEFAULT_PLAN_EXTENTION = ".plan";
	private Class<?> klass;
	private List<Plan> listPlans = new ArrayList<Plan>();

	private PlanParser planParser = new PlanParser();
	private PlanRunner planRunner;

	public ExenseTestRunner(Class<?> klass) throws InitializationError {
		super(klass);

		this.klass = klass;
		try {
			initListPlans();
			initPlanRunner();
		} catch (Exception e) {
			throw new InitializationError(e);
		}
	}

	private void initPlanRunner() throws Exception {

		List<Class<?>> listClass = new ArrayList<Class<?>>();
		Class<?> current = klass;
		do {
			listClass.add(current);
			current = current.getSuperclass();
		} while (current != Object.class);

		planRunner = new LocalPlanRunner(listClass) {
			private Map<String, String> executionParameters;
			
			@Override
			protected ExecutionContext buildExecutionContext() {
				ExecutionContext context = super.buildExecutionContext();
				
				executionParameters.forEach((k,v) -> {
					context.getVariablesManager().putVariable(context.getReport(), k, v);
				});
				
				return context;
			}
			
			@Override
			public PlanRunnerResult run(Plan plan, Map<String, String> executionParameters) {
				this.executionParameters = executionParameters;
				return super.run(plan);
			}
		};
	}

	private void initListPlans() throws Exception {
		PlanNames plans;

		if ((plans = klass.getAnnotation(PlanNames.class)) != null) {
			// explicit list of plans:
			for (String name : plans.value()) {
				addPlan(name);
			}
		} else {
			// all *.plans files in the package:
			URL url = klass.getResource(".");
			File folder = new File(url.getFile());
			for (File f : folder.listFiles()) {
				if (f.getName().endsWith(DEFAULT_PLAN_EXTENTION)) {
					addPlan(f.getName());
				}
			}
			if (listPlans.isEmpty()) {
				// one plan per keyword:
				for (Method method : klass.getMethods()) {
					if (method.isAnnotationPresent(Keyword.class)) {
						PlanBuilder planBuilder = PlanBuilder.create();

						planBuilder.startBlock(BaseArtefacts.sequence());

						planBuilder.add(FunctionArtefacts.keyword(method.getName()));

						planBuilder.endBlock();

						Plan plan = planBuilder.build();
						setPlanName(plan, method.getName());
						listPlans.add(plan);
					}
				}
			}
			if (listPlans.isEmpty()) {
				throw new InitializationError("No plans or keywords found");
			}
		}
	}

	private void addPlan(String name) throws Exception {
		URL planURL = klass.getResource(name);
		if (planURL == null) {
			throw new Exception("Plan '" + name + "' is not found for class " + klass.getName());
		}

		File f = new File(planURL.getPath());

		Plan plan = planParser.parse(new FileInputStream(f));
		setPlanName(plan, name);
		
		listPlans.add(plan);
	}

	@Override
	protected Description describeChild(Plan plan) {
		return Description.createTestDescription(klass, getPlanName(plan));
	}

	@Override
	protected void runChild(Plan plan, RunNotifier notifier) {
		Description desc = Description.createTestDescription(klass, getPlanName(plan));

		notifier.fireTestStarted(desc);

		try {
			Map<String,String> executionParameters = new HashMap<String, String>();
			Parameters params;
			if ((params = klass.getAnnotation(Parameters.class)) != null) {
				String key = null;
				for (String param: params.values()) {
					if (key==null) {
						key = param;
					} else {
						executionParameters.put(key,param);
						key = null;
					}
				}
			}
			
			PlanRunnerResult res = planRunner.run(plan,executionParameters);
			
			if (res.getResult() != ReportNodeStatus.PASSED) {
				notifier.fireTestFailure(
						new Failure(desc, new AssertionError("Plan " + getPlanName(plan) + " failed.")));
				// res.getResult().toString())));
			}
		} catch (Exception e) {
			notifier.fireTestFailure(new Failure(desc, e));
		}
		notifier.fireTestFinished(desc);
	}

	@Override
	protected List<Plan> getChildren() {
		return listPlans;
	}

	private String getPlanName(Plan plan) {
		return plan.getAttributes().get(AbstractArtefact.NAME);
	}

	private void setPlanName(Plan plan, String name) {
		Map<String, String> attributes = new HashMap<>();
		attributes.put(AbstractArtefact.NAME, name);
		plan.setAttributes(attributes);
	}
}
