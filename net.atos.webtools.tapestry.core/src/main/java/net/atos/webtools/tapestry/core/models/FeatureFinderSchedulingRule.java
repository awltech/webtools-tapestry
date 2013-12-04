package net.atos.webtools.tapestry.core.models;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * Scheduling rule that will ensure that two jobs will not be executed at the same time (to prevent from locks)
 * 
 * @author mvanbesien
 * @since 1.2
 *
 */
public class FeatureFinderSchedulingRule implements ISchedulingRule {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(org.eclipse.core.runtime.jobs.ISchedulingRule)
	 */
	@Override
	public boolean contains(ISchedulingRule rule) {
		return rule == this;
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
