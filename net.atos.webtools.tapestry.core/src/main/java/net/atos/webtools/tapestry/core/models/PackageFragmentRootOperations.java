package net.atos.webtools.tapestry.core.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;

/**
 * Static helping class for handling Package Fragment Roots lists intersections.
 * 
 * @author mvanbesien
 * 
 */
public class PackageFragmentRootOperations {

	/**
	 * Private constructor
	 */
	private PackageFragmentRootOperations() {
	}

	/**
	 * Returns the intersection of the two collections provided as parameters.
	 * 
	 * @param firstBunch
	 * @param secondBunch
	 * @return
	 */
	public static <T> Collection<T> intersect(Collection<T> firstBunch, Collection<T> secondBunch) {
		Collection<T> temp = new ArrayList<T>(firstBunch);
		temp.retainAll(secondBunch);
		return temp;
	}

	/**
	 * Intersects the java project with the resource delta.
	 * 
	 * The aim is to isolate all the IPackageFragmentRoots of the resource
	 * delta, and to return the intersection of them and the ones of the Java
	 * Project.
	 * 
	 * @param project
	 * @param resourceDelta
	 * @return
	 * @throws CoreException
	 */
	public static IPackageFragmentRoot[] intersect(IJavaProject project, IResourceDelta resourceDelta)
			throws CoreException {

		final Collection<IPackageFragmentRoot> projectFragmentRoots = toList(project.getAllPackageFragmentRoots());
		final Collection<IPackageFragmentRoot> deltaFragmentRoots = new ArrayList<IPackageFragmentRoot>();

		resourceDelta.accept(new IResourceDeltaVisitor() {

			@Override
			public boolean visit(IResourceDelta delta) throws CoreException {
				IResource resource = delta.getResource();
				IJavaElement javaElement = JavaCore.create(resource);
				if (javaElement instanceof IPackageFragmentRoot) {
					deltaFragmentRoots.add((IPackageFragmentRoot) javaElement);
					return false;
				}
				return true;
			}
		});

		Collection<IPackageFragmentRoot> intersection = intersect(projectFragmentRoots, deltaFragmentRoots);
		return toArray(intersection);
	}

	/**
	 * Transforms an array to a modifiable list.
	 * 
	 * @param array
	 * @return
	 */
	private static <T> Collection<T> toList(T[] array) {
		return new ArrayList<T>(Arrays.asList(array));
	}

	/**
	 * Transforms a list of Package Fragment Roots to an array
	 * 
	 * @param allPackageFragmentRoots
	 * @return
	 */
	private static IPackageFragmentRoot[] toArray(Collection<IPackageFragmentRoot> allPackageFragmentRoots) {
		return allPackageFragmentRoots.toArray(new IPackageFragmentRoot[allPackageFragmentRoots.size()]);
	}

}
