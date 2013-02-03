package net.atos.webtools.tapestry.core.models.features;

import net.atos.webtools.tapestry.core.models.ProjectModel;

import org.eclipse.jdt.core.IType;

/**
 * The model of a service, this is basically an {@link AbstractFeatureModel}
 * 
 * @author a160420
 *
 */
public class ServiceModel extends AbstractFeatureModel{

	public ServiceModel(String prefix, IType type, ProjectModel projectModel, String source, String subPackage) {
		super(prefix, type, projectModel, source, subPackage);
	}

}
