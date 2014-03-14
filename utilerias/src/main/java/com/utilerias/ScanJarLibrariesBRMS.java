package com.utilerias;

import java.io.File;

/**
 * 
 * @author asalas
 *
 */
public class ScanJarLibrariesBRMS {

	private static final String PATH_JAR_LIBRARIES = "/home/asalas/Desktop/brms_maven/libraries";
	private File jarLibrary;
	private StringBuilder stb;
	
	public void createMvnCommandInstallJar(String pathDirectory) {

		this.jarLibrary = new File(pathDirectory);
		File[] listFiles = this.jarLibrary.listFiles();

		// patron para crear el comando que agrega una libreria al repositorio local de maven
		// mvn install:install-file -Dfile=nombre_archivo.jar -DgroupId=mx.com.interware.brms -DartifactId=nombre_archivo -Dversion=version_archivo -Dpackaging=jar
		
		for (File fileEntry : listFiles) {
			if (fileEntry.isDirectory()) {
				createMvnCommandInstallJar(fileEntry.getAbsolutePath());
			} else {
				// se encontro un jar se prepara el stb para ser creado el
				// comando
				String fileName = fileEntry.getName();

				if (fileName.endsWith(".jar")) {
					this.stb = new StringBuilder();

					final int indexDigit = getIndexDigit(fileName);
					
					String nameFile = getNameFile(fileName, indexDigit);
					String versionFile = getVersionFile(fileName, indexDigit);

					this.stb.append("mvn install:install-file -Dfile=");
					this.stb.append(fileName);
					this.stb.append(" -DgroupId=mx.com.interware.brms -DartifactId=");
					this.stb.append(nameFile);				
					
					this.stb.append(" -Dversion=");
					if (versionFile != null) {
						this.stb.append(versionFile);
					} else {
						this.stb.append("1.0");
					}				
					
					this.stb.append(" -Dpackaging=jar");

					System.out.println(this.stb.toString());
				}
			}
		}

	}
	
	
	public void createMvnDepedenciesFromJar(String pathDirectory) {

		this.jarLibrary = new File(pathDirectory);
		File[] listFiles = this.jarLibrary.listFiles();

		// patron para crear la depedencia de maven que se debe agrega al POM
		// <dependency>
		// 		<groupId>com.thoughtworks.xstream</groupId>
		// 		<artifactId>xstream</artifactId>
		//		<version>${cxf.version}</version>
		// </dependency>
		
		for (File fileEntry : listFiles) {
			if (fileEntry.isDirectory()) {
				createMvnDepedenciesFromJar(fileEntry.getAbsolutePath());
			} else {
				// se encontro un jar se prepara el stb para ser creado el
				// comando
				String fileName = fileEntry.getName();

				if (fileName.endsWith(".jar")) {
					this.stb = new StringBuilder();

					final int indexDigit = getIndexDigit(fileName);
					
					String nameFile = getNameFile(fileName, indexDigit);
					String versionFile = getVersionFile(fileName, indexDigit);

					this.stb.append("<dependency>");					
					this.stb.append("<groupId>mx.com.interware.brms</groupId>");					
					this.stb.append("<artifactId>");
					this.stb.append(nameFile);
					this.stb.append("</artifactId>");
					
					// this.stb.append("<version>");
					// if (versionFile != null) {
					// this.stb.append(versionFile);
					// } else {
					// this.stb.append("1.0");
					// }
					// this.stb.append("</version>");
					
					this.stb.append("<version>${interware.jbpm.version}</version>");
					
					this.stb.append("</dependency>");				
					
					
					
					String dependency = this.stb.toString();
					// "pretty print"
					// System.out.println(PrettyPrinter.prettyPrintXml(dependency));
					
					// impresion normal
					System.out.println(dependency);
					
				}
			}
		}

	}
	
	private final int getIndexDigit(String fileName) {
		int indexDigit = 0;

		if (fileName != null) {
			for (char c : fileName.toCharArray()) {
				if (Character.isDigit(c)) {
					indexDigit = fileName.indexOf(c);
					break;
				}
			}
		}

		return indexDigit;
	}
	
	private String getNameFile(String fileName, int indexDigit) {
		String nameFile;
		if (indexDigit > 0) {
			// TODO: aqui solo se valida que el caracter de separacion es un guion medio '-'
			
			Character caracterSeparador = fileName.charAt(indexDigit-1);
			
			if (caracterSeparador == '-') {
				nameFile = fileName.substring(0, indexDigit-1);
			} else {
				nameFile = fileName.substring(0, indexDigit);
			}
			
		} else {
			nameFile = removeFileExtension(fileName);
		}
		
		return nameFile;		
	}
	
	private String getVersionFile(String fileName, int indexDigit) {
		String versionFile;
		
		if (indexDigit > 0) {
			versionFile = fileName.substring(indexDigit, fileName.length());
			versionFile = removeFileExtension(versionFile);
		} else {
			versionFile = null;
		}
		
		return versionFile;
	}

	private String removeFileExtension(String fileNameWithExt) {
		if (fileNameWithExt != null && !fileNameWithExt.isEmpty()) {
			if (fileNameWithExt.endsWith(".jar")) {
				fileNameWithExt = fileNameWithExt.substring(0, fileNameWithExt.length() - 4);
			}
		}
		return fileNameWithExt;
	}
	
	public static void main(String[] args) {
		
		ScanJarLibrariesBRMS  scanner = new ScanJarLibrariesBRMS();
		System.out.println("COMANDOS DE MVN PARA AGREGAR LAS LIBRERIAS AL REPOSITORIO LOCAL");
		scanner.createMvnCommandInstallJar(PATH_JAR_LIBRARIES);
		System.out.println("DEPENDENCIAS QUE SE DEBEN AGREGAR AL ARCHIVO POM.XML");
		scanner.createMvnDepedenciesFromJar(PATH_JAR_LIBRARIES);
	}
	
}
