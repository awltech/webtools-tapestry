package net.atos.webtools.tapestry.core.models.features;

import net.atos.webtools.tapestry.core.models.ProjectModel;

import org.eclipse.jdt.core.IType;

/**
 * The model of a page, this is basically an {@link AbstractFeatureModel}
 * 
 * @author a160420
 *
 */
public class PageModel  extends AbstractFeatureModel {

	public PageModel(String prefix, IType type, ProjectModel projectModel, String source, String subPackage) {
		super(prefix, type, projectModel, source, subPackage);
	}

}
