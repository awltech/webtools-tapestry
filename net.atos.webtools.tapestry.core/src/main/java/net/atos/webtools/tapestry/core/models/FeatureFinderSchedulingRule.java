package net.atos.webtools.tapestry.core.models;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * Scheduling rule to ensure that two Feature Finder jobs are not started at the same time (prevents locks)
 * 
 * @author mvanbesien
 *
 */
public class FeatureFinderSchedulingRule implements ISchedulingRule {

	private final IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(org.eclipse.core.runtime.jobs.ISchedulingRule)
	 */
	@Override
	public boolean contains(ISchedulingRule rule) {
		if (rule instanceof IResource) {
			return this.workspaceRoot.contains(rule);
		}
		return rule instanceof FeatureFinderSchedulingRule;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(org.eclipse.core.runtime.jobs.ISchedulingRule)
	 */
	@Override
	public boolean isConflicting(ISchedulingRule rule) {
		return rule == this;
	}

}
