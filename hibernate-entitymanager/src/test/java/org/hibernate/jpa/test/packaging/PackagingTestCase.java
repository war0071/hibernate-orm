/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2009-2011, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.jpa.test.packaging;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ExplodedExporter;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;

import org.hibernate.jpa.test.Cat;
import org.hibernate.jpa.test.Distributor;
import org.hibernate.jpa.test.Item;
import org.hibernate.jpa.test.Kitten;
import org.hibernate.jpa.test.pack.cfgxmlpar.Morito;
import org.hibernate.jpa.test.pack.defaultpar.ApplicationServer;
import org.hibernate.jpa.test.pack.defaultpar.IncrementListener;
import org.hibernate.jpa.test.pack.defaultpar.Lighter;
import org.hibernate.jpa.test.pack.defaultpar.Money;
import org.hibernate.jpa.test.pack.defaultpar.Mouse;
import org.hibernate.jpa.test.pack.defaultpar.OtherIncrementListener;
import org.hibernate.jpa.test.pack.defaultpar.Version;
import org.hibernate.jpa.test.pack.defaultpar_1_0.ApplicationServer1;
import org.hibernate.jpa.test.pack.defaultpar_1_0.IncrementListener1;
import org.hibernate.jpa.test.pack.defaultpar_1_0.Lighter1;
import org.hibernate.jpa.test.pack.defaultpar_1_0.Money1;
import org.hibernate.jpa.test.pack.defaultpar_1_0.Mouse1;
import org.hibernate.jpa.test.pack.defaultpar_1_0.Version1;
import org.hibernate.jpa.test.pack.excludehbmpar.Caipirinha;
import org.hibernate.jpa.test.pack.explodedpar.Carpet;
import org.hibernate.jpa.test.pack.explodedpar.Elephant;
import org.hibernate.jpa.test.pack.externaljar.Scooter;
import org.hibernate.jpa.test.pack.spacepar.Bug;
import org.hibernate.jpa.test.pack.various.Airplane;
import org.hibernate.jpa.test.pack.various.Seat;

import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;

import static org.junit.Assert.fail;

/**
 * @author Hardy Ferentschik
 */
public abstract class PackagingTestCase extends BaseCoreFunctionalTestCase {
	protected static ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
	protected static ClassLoader bundleClassLoader;
	protected static File packageTargetDir;

	static {
		// get a URL reference to something we now is part of the classpath (us)
		URL myUrl = originalClassLoader.getResource(
				PackagingTestCase.class.getName().replace( '.', '/' ) + ".class"
		);
		// this is assuming that there is a target directory
		int index = myUrl.getFile().lastIndexOf( "target" );
		if ( index == -1 ) {
			fail( "Unable to setup packaging test" );
		}

		String baseDirPath = myUrl.getFile().substring( 0, index );
		File baseDir = new File( baseDirPath );

		File testPackagesDir = new File( baseDir, "target/bundles" );
		try {
			bundleClassLoader = new URLClassLoader( new URL[] { testPackagesDir.toURL() }, originalClassLoader );
		}
		catch ( MalformedURLException e ) {
			fail( "Unable to build custom class loader" );
		}
		packageTargetDir = new File( baseDir, "target/packages" );
		packageTargetDir.mkdirs();
	}

	@Before
	public void prepareTCCL() {
		// add the bundle class loader in order for ShrinkWrap to build the test package
		Thread.currentThread().setContextClassLoader( bundleClassLoader );
	}

	@After
	public void resetTCCL() throws Exception {
		// reset the classloader
		Thread.currentThread().setContextClassLoader( originalClassLoader );
	}

	protected void addPackageToClasspath(File... files) throws MalformedURLException {
		List<URL> urlList = new ArrayList<URL>();
		for ( File file : files ) {
			urlList.add( file.toURL() );
		}
		URLClassLoader classLoader = new URLClassLoader(
				urlList.toArray( new URL[urlList.size()] ), originalClassLoader
		);
		Thread.currentThread().setContextClassLoader( classLoader );
	}

	protected void addPackageToClasspath(URL... urls) throws MalformedURLException {
		List<URL> urlList = new ArrayList<URL>();
		urlList.addAll( Arrays.asList( urls ) );
		URLClassLoader classLoader = new URLClassLoader(
				urlList.toArray( new URL[urlList.size()] ), originalClassLoader
		);
		Thread.currentThread().setContextClassLoader( classLoader );
	}

	protected File buildDefaultPar() {
		String fileName = "defaultpar.par";
		JavaArchive archive = ShrinkWrap.create(  JavaArchive.class, fileName );
		archive.addClasses(
				ApplicationServer.class,
				Lighter.class,
				Money.class,
				Mouse.class,
				OtherIncrementListener.class,
				IncrementListener.class,
				Version.class
		);
		ArchivePath path = ArchivePaths.create( "META-INF/orm.xml" );
		archive.addAsResource( "defaultpar/META-INF/orm.xml", path );

		path = ArchivePaths.create( "META-INF/persistence.xml" );
		archive.addAsResource( "defaultpar/META-INF/persistence.xml", path );

		path = ArchivePaths.create( "org/hibernate/jpa/test/pack/defaultpar/Mouse.hbm.xml" );
		archive.addAsResource( "defaultpar/org/hibernate/jpa/test/pack/defaultpar/Mouse.hbm.xml", path );

		path = ArchivePaths.create( "org/hibernate/jpa/test/pack/defaultpar/package-info.class" );
		archive.addAsResource( "org/hibernate/jpa/test/pack/defaultpar/package-info.class", path );


		File testPackage = new File( packageTargetDir, fileName );
		archive.as( ZipExporter.class ).exportTo ( testPackage, true );
		return testPackage;
	}

	protected File buildDefaultPar_1_0() {
		String fileName = "defaultpar_1_0.par";
		JavaArchive archive = ShrinkWrap.create(  JavaArchive.class,fileName );
		archive.addClasses(
				ApplicationServer1.class,
				Lighter1.class,
				Money1.class,
				Mouse1.class,
				IncrementListener1.class,
				Version1.class
		);
		ArchivePath path = ArchivePaths.create( "META-INF/orm.xml" );
		archive.addAsResource( "defaultpar_1_0/META-INF/orm.xml", path );

		path = ArchivePaths.create( "META-INF/persistence.xml" );
		archive.addAsResource( "defaultpar_1_0/META-INF/persistence.xml", path );

		path = ArchivePaths.create( "org/hibernate/jpa/test/pack/defaultpar_1_0/Mouse.hbm.xml" );
		archive.addAsResource( "defaultpar_1_0/org/hibernate/jpa/test/pack/defaultpar_1_0/Mouse1.hbm.xml", path );

		path = ArchivePaths.create( "org/hibernate/jpa/test/pack/defaultpar_1_0/package-info.class" );
		archive.addAsResource( "org/hibernate/jpa/test/pack/defaultpar_1_0/package-info.class", path );


		File testPackage = new File( packageTargetDir, fileName );
		archive.as( ZipExporter.class ).exportTo( testPackage, true );
		return testPackage;
	}

	protected File buildExplicitPar() {
		String fileName = "explicitpar.par";
		JavaArchive archive = ShrinkWrap.create( JavaArchive.class, fileName );
		archive.addClasses(
				Airplane.class,
				Seat.class,
				Cat.class,
				Kitten.class,
				Distributor.class,
				Item.class
		);

		ArchivePath path = ArchivePaths.create( "META-INF/orm.xml" );
		archive.addAsResource( "explicitpar/META-INF/orm.xml", path );

		path = ArchivePaths.create( "META-INF/persistence.xml" );
		archive.addAsResource( "explicitpar/META-INF/persistence.xml", path );

		File testPackage = new File( packageTargetDir, fileName );
		archive.as( ZipExporter.class ).exportTo( testPackage, true );
		return testPackage;
	}

	protected File buildExplodedPar() {
		String fileName = "explodedpar";
		JavaArchive archive = ShrinkWrap.create(  JavaArchive.class,fileName );
		archive.addClasses(
				Elephant.class,
				Carpet.class
		);

		ArchivePath path = ArchivePaths.create( "META-INF/persistence.xml" );
		archive.addAsResource( "explodedpar/META-INF/persistence.xml", path );

		path = ArchivePaths.create( "org/hibernate/jpa/test/pack/explodedpar/Elephant.hbm.xml" );
		archive.addAsResource( "explodedpar/org/hibernate/jpa/test/pack/explodedpar/Elephant.hbm.xml", path );

		path = ArchivePaths.create( "org/hibernate/jpa/test/pack/explodedpar/package-info.class" );
		archive.addAsResource( "org/hibernate/jpa/test/pack/explodedpar/package-info.class", path );

		File testPackage = new File( packageTargetDir, fileName );
		archive.as( ExplodedExporter.class ).exportExploded( packageTargetDir );
		return testPackage;
	}

	protected File buildExcludeHbmPar() {
		String fileName = "excludehbmpar.par";
		JavaArchive archive = ShrinkWrap.create( JavaArchive.class,fileName );
		archive.addClasses(
				Caipirinha.class
		);

		ArchivePath path = ArchivePaths.create( "META-INF/orm2.xml" );
		archive.addAsResource( "excludehbmpar/META-INF/orm2.xml", path );

		path = ArchivePaths.create( "META-INF/persistence.xml" );
		archive.addAsResource( "excludehbmpar/META-INF/persistence.xml", path );

		path = ArchivePaths.create( "org/hibernate/jpa/test/pack/excludehbmpar/Mouse.hbm.xml" );
		archive.addAsResource( "excludehbmpar/org/hibernate/jpa/test/pack/excludehbmpar/Mouse.hbm.xml", path );

		File testPackage = new File( packageTargetDir, fileName );
		archive.as( ZipExporter.class ).exportTo( testPackage, true );
		return testPackage;
	}

	protected File buildCfgXmlPar() {
		String fileName = "cfgxmlpar.par";
		JavaArchive archive = ShrinkWrap.create( JavaArchive.class,fileName );
		archive.addClasses(
				Morito.class,
				Item.class
		);

		ArchivePath path = ArchivePaths.create( "META-INF/persistence.xml" );
		archive.addAsResource( "cfgxmlpar/META-INF/persistence.xml", path );

		path = ArchivePaths.create( "org/hibernate/jpa/test/pack/cfgxmlpar/hibernate.cfg.xml" );
		archive.addAsResource( "cfgxmlpar/org/hibernate/jpa/test/pack/cfgxmlpar/hibernate.cfg.xml", path );

		File testPackage = new File( packageTargetDir, fileName );
		archive.as( ZipExporter.class ).exportTo( testPackage, true );
		return testPackage;
	}

	protected File buildSpacePar() {
		String fileName = "space par.par";
		JavaArchive archive = ShrinkWrap.create( JavaArchive.class, fileName );
		archive.addClasses(
				Bug.class
		);

		ArchivePath path = ArchivePaths.create( "META-INF/persistence.xml" );
		archive.addAsResource( "space par/META-INF/persistence.xml", path );

		File testPackage = new File( packageTargetDir, fileName );
		archive.as( ZipExporter.class ).exportTo( testPackage, true );
		return testPackage;
	}

	protected File buildOverridenPar() {
		String fileName = "overridenpar.jar";
		JavaArchive archive = ShrinkWrap.create( JavaArchive.class, fileName );
		archive.addClasses(
				org.hibernate.jpa.test.pack.overridenpar.Bug.class
		);

		ArchivePath path = ArchivePaths.create( "META-INF/persistence.xml" );
		archive.addAsResource( "overridenpar/META-INF/persistence.xml", path );

		path = ArchivePaths.create( "overridenpar.properties" );
		archive.addAsResource( "overridenpar/overridenpar.properties", path );

		File testPackage = new File( packageTargetDir, fileName );
		archive.as( ZipExporter.class ).exportTo( testPackage, true );
		return testPackage;
	}

	protected File buildExternalJar() {
		String fileName = "externaljar.jar";
		JavaArchive archive = ShrinkWrap.create( JavaArchive.class, fileName );
		archive.addClasses(
				Scooter.class
		);

		ArchivePath path = ArchivePaths.create( "META-INF/orm.xml" );
		archive.addAsResource( "externaljar/META-INF/orm.xml", path );

		File testPackage = new File( packageTargetDir, fileName );
		archive.as( ZipExporter.class ).exportTo( testPackage, true );
		return testPackage;
	}

	protected File buildWar() {
		String fileName = "war.war";
		WebArchive archive = ShrinkWrap.create( WebArchive.class, fileName );
		archive.addClasses(
				org.hibernate.jpa.test.pack.war.ApplicationServer.class,
				org.hibernate.jpa.test.pack.war.IncrementListener.class,
				org.hibernate.jpa.test.pack.war.Lighter.class,
				org.hibernate.jpa.test.pack.war.Money.class,
				org.hibernate.jpa.test.pack.war.Mouse.class,
				org.hibernate.jpa.test.pack.war.OtherIncrementListener.class,
				org.hibernate.jpa.test.pack.war.Version.class
		);

		ArchivePath path = ArchivePaths.create( "WEB-INF/classes/META-INF/orm.xml" );
		archive.addAsResource( "war/WEB-INF/classes/META-INF/orm.xml", path );

		path = ArchivePaths.create( "WEB-INF/classes/META-INF/persistence.xml" );
		archive.addAsResource( "war/WEB-INF/classes/META-INF/persistence.xml", path );

		path = ArchivePaths.create( "WEB-INF/classes/org/hibernate/jpa/test/pack/war/Mouse.hbm.xml" );
		archive.addAsResource( "war/WEB-INF/classes/org/hibernate/jpa/test/pack/war/Mouse.hbm.xml", path );

		File testPackage = new File( packageTargetDir, fileName );
		archive.as( ZipExporter.class ).exportTo( testPackage, true );
		return testPackage;
	}

	protected File buildNestedEar(File includeFile) {
		String fileName = "nestedjar.ear";
		JavaArchive archive = ShrinkWrap.create( JavaArchive.class, fileName );
		archive.addAsResource( includeFile );

		File testPackage = new File( packageTargetDir, fileName );
		archive.as( ZipExporter.class ).exportTo( testPackage, true );
		return testPackage;
	}

	protected File buildNestedEarDir(File includeFile) {
		String fileName = "nesteddir.ear";
		JavaArchive archive = ShrinkWrap.create( JavaArchive.class, fileName );
		archive.addAsResource( includeFile );

		File testPackage = new File( packageTargetDir, fileName );
		archive.as( ExplodedExporter.class ).exportExploded( packageTargetDir );
		return testPackage;
	}

}


