package client.interpreter;


import java.util.ArrayList;
import java.util.Stack;

import client.ZClipper;
import client.interpreter.LineBasedReader;
import geometry.*;
import javafx.geometry.Point3D;
import line.LineRenderer;
import client.Clipper;
import client.DepthCueingDrawable;
import client.RendererTrio;
import polygon.Polygon;
import polygon.PolygonRenderer;
import polygon.Shader;
import shading.Light;
import shading.FlatShading;
import shading.GouraudShading;
import shading.PhongRender;
import windowing.drawable.Drawable;
import windowing.graphics.Color;
import windowing.graphics.Dimensions;


public class SimpInterpreter {
	private static final int NUM_TOKENS_FOR_POINT = 3;
	private static final int NUM_TOKENS_FOR_COMMAND = 1;
	private static final int NUM_TOKENS_FOR_COLORED_VERTEX = 6;
	private static final int NUM_TOKENS_FOR_UNCOLORED_VERTEX = 3;
	private static final char COMMENT_CHAR = '#';
	private RenderStyle renderStyle;

	//private Transformation CTM;
	// If I make CTM as a Transformation, its number will be very larege, I don't know why. I have to make Transformation as static
	// and make CTM as an array. Now it works.
	private static double CTM[][] = new double [4][4];
	//private Stack<Transformation> CTMStack = new Stack<Transformation>();
	private Stack<double[][]> CTMStack = new Stack<double[][]>();
	private Transformation worldToScreen;

	private static int WORLD_LOW_X = -100;
	private static int WORLD_HIGH_X = 100;
	private static int WORLD_LOW_Y = -100;
	private static int WORLD_HIGH_Y = 100;

	private LineBasedReader reader;
	private Stack<LineBasedReader> readerStack;

	private Color defaultColor = Color.WHITE;
	private Color ambientLight = Color.WHITE;
	private Color surfaceColor = defaultColor;


	private Drawable drawable;
	private Drawable depthCueingDrawable;

	private LineRenderer lineRenderer;
	private PolygonRenderer filledRenderer;
	private PolygonRenderer wireframeRenderer;
	private Transformation cameraToScreen;
	private Clipper clipper;

	// add things here

	public static double[][] inverseCamera;
	public static double CTM_ForWire[][] = Transformation.identity().getMatrix();
	public static boolean NoWire = true;
	public static boolean backSide = false;
	private Color DarkColor = Color.WHITE;

	private double z_near = -Double.MAX_VALUE;
	private double z_far = -Double.MAX_VALUE;

	private double x_low;
	private double y_low;
	private double x_high;
	private double y_high;
	private double hither;
	private double yon;
	private boolean has_camera;
	private double SpecularCoefficient = 0.3;
	private double SpecularExp = 8;

	public static double camera_height = 0;
	public static double camera_width = 0;

	private Color depthColor= Color.BLACK;
	private Color ObjectColor = Color.WHITE;

	// for shading:
	private boolean FlatShading = false;
	private boolean PhongShading = false;
	private boolean GouraudShading = false;
	private String ShadingStyle = "None";
	private Color LightIntensity;
	private double attenuationA, attenuationB;
	private Vertex3D LightLocation;
	private Light lightBulb;
	private ArrayList<Light> lightList;

	private FlatShading flat;
	private GouraudShading gouraud;
	private PhongRender Phong;
	//private PhongShading phong;







	public enum RenderStyle {
		FILLED,
		WIREFRAME;
	}

	private static double[][] toworld;

	public SimpInterpreter(String filename,
						   Drawable drawable,
						   RendererTrio renderers) {
		x_low = -100;
		y_low = -100;
		x_high = 100;
		y_high = 100;
		hither = 0;
		yon = -200;
		has_camera = false;
		NoWire = true;
		inverseCamera = Transformation.identity().getMatrix();

		camera_height = 0;
		camera_width = 0;


		this.drawable = drawable;
		this.depthCueingDrawable = drawable;
		this.lineRenderer = renderers.getLineRenderer();
		this.filledRenderer = renderers.getFilledRenderer();
		this.wireframeRenderer = renderers.getWireframeRenderer();
		this.defaultColor = Color.WHITE;
		makeWorldToScreenTransform(drawable.getDimensions());
		reader = new LineBasedReader(filename);
		readerStack = new Stack<>();
		renderStyle = RenderStyle.FILLED;
		//CTM = Transformation.identity();
		CTM = Transformation.identity().getMatrix();
		lightList = new ArrayList<Light>();
	}

	private void makeWorldToScreenTransform(Dimensions dimensions) {
		// TODO: fill this in

		double height = dimensions.getHeight();
		double width = dimensions.getWidth();

		double WorldH = height / (y_high - y_low);
		double WorldW = width / (x_high - x_low);
		double WorldZ = 1.0;

		double translationX = 0.0 - x_low;
		double translationY = 0.0 - y_low;
		double translationZ = 0.0;

		double translate_height = 100;
		double translate_width = 100;

		double [][] toWindowSpace = Transformation.identity().getMatrix();

		toWindowSpace[0][3] = translate_width;
		toWindowSpace[1][3] = translate_height;

		//should not use static functions.
		double value;
		double temp[][] = Transformation.identity().getMatrix();
		temp[0][0] = WorldH;
		temp[1][1] = WorldW;
		double[][] NewArray = Transformation.identity().getMatrix();
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				value = 0;
				for(int k = 0; k < 4; k++) {
					value += temp[i][k] * toWindowSpace[k][j] ;
				}
				NewArray[i][j] = value;
			}
		}
		//worldToScreen.setValue(NewArray);
		toworld = NewArray;
/*
		double value;
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				value = 0;
				for(int k = 0; k < 4; k++) {
					value += temp[i][k] * toWindowSpace[k][j] ;
				}
				NewArray[i][j] = value;
			}
		}
		toWindowSpace = NewArray;
*/

	}

	public void interpret() {
		while(reader.hasNext() ) {
			String line = reader.next().trim();
			interpretLine(line);
			while(!reader.hasNext()) {
				if(readerStack.isEmpty()) {
					return;
				}
				else {
					reader = readerStack.pop();
				}
			}
		}
	}
	public void interpretLine(String line) {
		if(!line.isEmpty() && line.charAt(0) != COMMENT_CHAR) {
			String[] tokens = line.split("[ \t,()]+");
			if(tokens.length != 0) {
				interpretCommand(tokens);
			}
		}
	}
	private void interpretCommand(String[] tokens) {
		switch(tokens[0]) {
			case "{" :      push();   break;
			case "}" :      pop();    break;
			case "wire" :   wire();   break;
			case "filled" : filled(); break;

			case "file" :		interpretFile(tokens);		break;
			case "scale" :		interpretScale(tokens);		break;
			case "translate" :	interpretTranslate(tokens);	break;
			case "rotate" :		interpretRotate(tokens);	break;
			case "line" :		interpretLine(tokens);		break;
			case "polygon" :	interpretPolygon(tokens);	break;
			case "camera" :		interpretCamera(tokens);	break;
			case "surface" :	interpretSurface(tokens);	break;
			case "ambient" :	interpretAmbient(tokens);	break;
			case "depth" :		interpretDepth(tokens);		break;
			case "obj" :		interpretObj(tokens);		break;
			case "flat" :		interpretFlat();			break;
			case "gouraud" :	interpretGouraud();			break;
			case "phong" :		interpretPhong();			break;
			case "light" :		interpretLight(tokens);		break;

			default :
				System.err.println("bad input line: " + tokens);
				break;
		}
	}

	private void push() {

		// TODO: finish this method
		CTMStack.push(CTM);
	}
	private void pop() {
		CTM = CTMStack.pop();
		// TODO: finish this method
	}
	private void wire() {
		renderStyle = RenderStyle.WIREFRAME;
		NoWire = false;
		// TODO: finish this method
	}
	private void filled() {
		renderStyle = RenderStyle.FILLED;
		NoWire = true;
		// TODO: finish this method
	}

	// this one is complete.
	private void interpretFile(String[] tokens) {
		String quotedFilename = tokens[1];
		int length = quotedFilename.length();
		assert quotedFilename.charAt(0) == '"' && quotedFilename.charAt(length-1) == '"';
		String filename = quotedFilename.substring(1, length-1);
		file(filename + ".simp");
	}
	private void file(String filename) {
		readerStack.push(reader);
		reader = new LineBasedReader(filename);
	}

	private void interpretScale(String[] tokens) {
		double sx = cleanNumber(tokens[1]);
		double sy = cleanNumber(tokens[2]);
		double sz = cleanNumber(tokens[3]);
		// TODO: finish this method
		//System.out.println("interpretScale");


		Transformation trans = Transformation.identity();
		double tempCTM[][] = new double[4][4];
		trans.setValue(0, 0, sx);
		trans.setValue(1, 1, sy);
		trans.setValue(2, 2, sz);

		// multiply transformation matrix with CTM
		double value;
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				value = 0;
				for(int k = 0; k < 4; k++) {
					//value += CTM.getMatrix()[i][k] * trans.getMatrix()[k][j] ;
					value += CTM[i][k] * trans.getMatrix()[k][j] ;
				}
				//CTM.setValue(i,j,value);
				tempCTM[i][j] = value;
			}
		}
		 //CTM.setValue(tempCTM);
		CTM = tempCTM;
		// problems here. I dont know why
		// if we do ctm here is ok
		// if we set ctm inside, it will only show one

		//System.out.println("interpretScale2");
	}
	private void interpretTranslate(String[] tokens) {
		double tx = cleanNumber(tokens[1]);
		double ty = cleanNumber(tokens[2]);
		double tz = cleanNumber(tokens[3]);
		//System.out.println("interpretTranslate");
		// TODO: finish this method

		// translate matrix
		Transformation trans = Transformation.identity();
		double tempCTM[][] = new double[4][4];
		trans.setValue(0, 3, tx);
		trans.setValue(1, 3, ty);
		trans.setValue(2, 3, tz);

		double value;
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				value = 0;
				for(int k = 0; k < 4; k++) {
					value += CTM[i][k] * trans.getMatrix()[k][j] ;
					//value += CTM.getMatrix()[i][k] * trans.getMatrix()[k][j] ;
				}
				//CTM.setValue(i,j,value);
				tempCTM[i][j] = value;
			}
		}
		//CTM.setValue(tempCTM);
		CTM = tempCTM;
		//System.out.println("interpretTranslate2");
	}
	private void interpretRotate(String[] tokens) {
		String axisString = tokens[1];
		double angleInDegrees = cleanNumber(tokens[2]);

		// TODO: finish this method
		//System.out.println("interpretRotate");
		double tempCTM[][] = new double[4][4];
		Transformation trans = Transformation.identity();

		if(axisString.equals("X")) {
			trans.setValue(1, 1, Math.cos(Math.toRadians(angleInDegrees)));
			trans.setValue(2, 1, Math.sin(Math.toRadians(angleInDegrees)));
			trans.setValue(1, 2, (-1) * Math.sin(Math.toRadians(angleInDegrees)));
			trans.setValue(2, 2, Math.cos(Math.toRadians(angleInDegrees)));
		}
		else if(axisString.equals("Y")) {
			trans.setValue(0, 0, Math.cos(Math.toRadians(angleInDegrees)));
			trans.setValue(0, 2, Math.sin(Math.toRadians(angleInDegrees)));
			trans.setValue(2, 0, (-1) * Math.sin(Math.toRadians(angleInDegrees)));
			trans.setValue(2, 2, Math.cos(Math.toRadians(angleInDegrees)));
		}
		else if(axisString.equals("Z")) {
			trans.setValue(0, 0, Math.cos(Math.toRadians(angleInDegrees)));
			trans.setValue(0, 1, (-1) * Math.sin(Math.toRadians(angleInDegrees)));
			trans.setValue(1, 0, Math.sin(Math.toRadians(angleInDegrees)));
			trans.setValue(1, 1, Math.cos(Math.toRadians(angleInDegrees)));
		}

		double value;
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				value = 0;
				for(int k = 0; k < 4; k++) {
					value += CTM[i][k] * trans.getMatrix()[k][j] ;
					//value += CTM.getMatrix()[i][k] * trans.getMatrix()[k][j] ;
				}
				//CTM.setValue(i,j,value);
				tempCTM[i][j] = value;
			}
		}
		CTM = tempCTM;
		//CTM.setValue(tempCTM);
		//System.out.println("interpretRotate2");
	}
	private static double cleanNumber(String string) {
		return Double.parseDouble(string);
	}

	private enum VertexColors {
		COLORED(NUM_TOKENS_FOR_COLORED_VERTEX),
		UNCOLORED(NUM_TOKENS_FOR_UNCOLORED_VERTEX);

		private int numTokensPerVertex;

		private VertexColors(int numTokensPerVertex) {
			this.numTokensPerVertex = numTokensPerVertex;
		}
		public int numTokensPerVertex() {
			return numTokensPerVertex;
		}
	}
	private void interpretLine(String[] tokens) {
		Vertex3D[] vertices = interpretVertices(tokens, 2, 1);

		// TODO: finish this method
		line(vertices[0], vertices[1]);
	}
	private void interpretPolygon(String[] tokens) {
		Vertex3D[] vertices = interpretVertices(tokens, 3, 1);

		// TODO: finish this method
		//new_vertices = transformTo_Z_deepthS(new_vertices);



		if(FlatShading) {
			Polygon PolygonWithOutLight = Polygon.make(vertices[0], vertices[1], vertices[2]);
			flat = new FlatShading(PolygonWithOutLight);
			flat.addLights(lightList, SpecularCoefficient, SpecularExp, ObjectColor);
			Polygon PolygonWithLight = flat.shade(PolygonWithOutLight);
			vertices[0] = PolygonWithLight.get(0);
			vertices[1] = PolygonWithLight.get(1);
			vertices[2] = PolygonWithLight.get(2);
		}else if(GouraudShading){
			Polygon PolygonWithOutLight = Polygon.make(vertices[0], vertices[1], vertices[2]);
			gouraud = new GouraudShading();
			gouraud.addLights(lightList, SpecularCoefficient, SpecularExp, ObjectColor);
			vertices[0] = gouraud.shade(PolygonWithOutLight, vertices[0]);
			vertices[1] = gouraud.shade(PolygonWithOutLight, vertices[1]);
			vertices[2] = gouraud.shade(PolygonWithOutLight, vertices[2]);
		}else{
			Polygon PolygonWithOutLight = Polygon.make(vertices[0], vertices[1], vertices[2]);
			Phong = new PhongRender();
			Phong.addLights(PolygonWithOutLight, lightList, SpecularCoefficient, SpecularExp, ObjectColor);
		}



		//----------- back face cut:
//		Vertex3D[] new_vertices = vertices;
//		double a1 = new_vertices[1].getX() - new_vertices[0].getX();
//		double a2 = new_vertices[1].getY() - new_vertices[0].getY();
//		double a3 = new_vertices[1].getZ() - new_vertices[0].getZ();
//		double b1 = new_vertices[2].getX() - new_vertices[0].getX();
//		double b2 = new_vertices[2].getY() - new_vertices[0].getY();
//		double b3 = new_vertices[2].getZ() - new_vertices[0].getZ();
//		double c1 = a2*b3-a3*b2;
//		double c2 = a3*b1-a1*b3;
//		double c3 = a1*b2-a2*b1;
//		double A1 = new_vertices[0].getX();
//		double A2 = new_vertices[0].getY();
//		double A3 = new_vertices[0].getZ();
//		double isClockwise = c1*A1 + c2*A2 + c3*A3;
//		boolean Piercing = (new_vertices[0].getZ() > -1 || new_vertices[1].getZ() > -1 || new_vertices[2].getZ() > -1);
		//--------------------------------------------------------------------------------------------------------------------

		//new_vertices = cut_vertices_zFar_and_near(new_vertices);
		ZClipper Z_clipper = new ZClipper(yon, hither, vertices[0], vertices[1], vertices[2]);
		if (Z_clipper.NewPolygonPoints.size() != 0) {
			ArrayList<Vertex3D> new_points = transformTo_Z_deepthS(Z_clipper.NewPolygonPoints);
			Clipper clipper = new Clipper(x_low, y_low, yon, x_high, y_high, hither, new_points);
			ArrayList<Vertex3D> points = clipper.NewPolygonPoints;
			if (points.size() > 0) {
				ArrayList<Vertex3D> Large_points = new ArrayList<Vertex3D>();
				for (Vertex3D visitor : points) {
					Vertex3D new_point = transformToCamera(visitor);
					new_point.setCmaeraPoint(visitor.getCameraPoint());
					Large_points.add(new_point);
				}
				int numberI = 1;
				int arraySize = Large_points.size() - 2;
				while (numberI <= arraySize) {
					Polygon triangle;
					triangle = Polygon.make(Large_points.get(numberI), Large_points.get(0), Large_points.get(numberI + 1));
					if ( NoWire ) {
						if (FlatShading || GouraudShading) {
							filledRenderer.drawPolygon(triangle, drawable);
						}else{
							Phong.drawPolygon(triangle, drawable);
						}
					}else if (NoWire){
						wireframeRenderer.drawPolygon(triangle, drawable);
					}
					numberI++;
				}
			}
		}



		//old code:

		/*		Polygon triangle = null;
		if (array_size == 6) {
			triangle = Polygon.make(Large_points.get(0),Large_points.get(1),Large_points.get(2),Large_points.get(3),Large_points.get(4),Large_points.get(5));
		}else if (array_size == 5) {
			triangle = Polygon.make(Large_points.get(0),Large_points.get(1),Large_points.get(2),Large_points.get(3),Large_points.get(4));
		}else if (array_size == 4) {
			triangle = Polygon.make(Large_points.get(0),Large_points.get(1),Large_points.get(2),Large_points.get(3));
		}else if (array_size == 3) {
			triangle = Polygon.make(Large_points.get(0),Large_points.get(1),Large_points.get(2));
		}
		if (array_size >= 3) {
			if (NoWire == true) {
				filledRenderer.drawPolygon(triangle, drawable);
			} else {
				wireframeRenderer.drawPolygon(triangle, drawable);
			}
		}*/

	}

	private Point3DH getNormalByCTM(double[] normal) {

		double current_point[] = new double[4];
		double temp[] = new double[4];
		current_point[0] = normal[0];
		current_point[1] = normal[1];
		current_point[2] = normal[2];
		current_point[3] = 1.0;

		for(int i = 0; i < 4; i++) {
			double value = 0;
			for(int j = 0; j < 4; j++) {
				value += inverseCamera[i][j] * current_point[j];
			}
			temp[i] = value;
		}
		current_point = temp;

		Point3DH ret = new Point3DH(current_point[0], current_point[1], current_point[2]);
		return ret;
	}

	public void interpretPolygonFromObj(Vertex3D V1, Vertex3D V2, Vertex3D V3) {
		Vertex3D[] vertices = new Vertex3D[3];
		vertices[0] = V1;
		vertices[1] = V2;
		vertices[2] = V3;
		for(int i = 0; i<3; i++){
			vertices[i] = interpretVertex(vertices[i]);
		}

		//Vertex3D[] new_vertices = transformToViewS(vertices);


		if(FlatShading) {
			Polygon PolygonWithOutLight = Polygon.make(vertices[0], vertices[1], vertices[2]);
			flat = new FlatShading(PolygonWithOutLight);
			flat.addLights(lightList, SpecularCoefficient, SpecularExp, ObjectColor);
			Polygon PolygonWithLight = flat.shade(PolygonWithOutLight);
			vertices[0] = PolygonWithLight.get(0);
			vertices[1] = PolygonWithLight.get(1);
			vertices[2] = PolygonWithLight.get(2);
		}else if(GouraudShading){
			Polygon PolygonWithOutLight = Polygon.make(vertices[0], vertices[1], vertices[2]);
			gouraud = new GouraudShading();
			gouraud.addLights(lightList, SpecularCoefficient, SpecularExp, ObjectColor);
			vertices[0] = gouraud.shade(PolygonWithOutLight, vertices[0]);
			vertices[1] = gouraud.shade(PolygonWithOutLight, vertices[1]);
			vertices[2] = gouraud.shade(PolygonWithOutLight, vertices[2]);
		} else{
			Polygon PolygonWithOutLight = Polygon.make(vertices[0], vertices[1], vertices[2]);
			Phong = new PhongRender();
			Phong.addLights(PolygonWithOutLight, lightList, SpecularCoefficient, SpecularExp, ObjectColor);
		}

		ZClipper Z_clipper = new ZClipper(yon, hither, vertices[0], vertices[1], vertices[2]);
		if (Z_clipper.NewPolygonPoints.size() != 0) {
			ArrayList<Vertex3D> new_points = transformTo_Z_deepthS(Z_clipper.NewPolygonPoints);
			Clipper clipper = new Clipper(x_low, y_low, yon, x_high, y_high, hither, new_points);
			ArrayList<Vertex3D> points = clipper.NewPolygonPoints;
			if (points.size() > 0) {
				ArrayList<Vertex3D> Large_points = new ArrayList<Vertex3D>();
				for (Vertex3D visitor : points) {
					Vertex3D new_point = transformToCamera(visitor);
					new_point.setCmaeraPoint(visitor.getCameraPoint());
					Large_points.add(new_point);
				}
				int numberI = 1;
				int arraySize = Large_points.size() - 2;
				while (numberI <= arraySize) {
					Polygon triangle;
					triangle = Polygon.make(Large_points.get(numberI), Large_points.get(0), Large_points.get(numberI + 1));
					if ( NoWire ) {
						if (FlatShading || GouraudShading) {
							filledRenderer.drawPolygon(triangle, drawable);
						}else{
							Phong.drawPolygon(triangle, drawable);
						}
					}else if (NoWire){
						wireframeRenderer.drawPolygon(triangle, drawable);
					}
					numberI++;
				}
			}
		}
	}

//	public boolean is_inside_the_screen(Vertex3D v){
//		if (v.getY() <= y_low || v.getY() >= y_high){
//			return false;
//		}else if (v.getY() <= y_low || v.getY() >= y_high
//	}




	public Vertex3D[] interpretVertices(String[] tokens, int numVertices, int startingIndex) {
		VertexColors vertexColors = verticesAreColored(tokens, numVertices);
		Vertex3D vertices[] = new Vertex3D[numVertices];

		for(int index = 0; index < numVertices; index++) {
			vertices[index] = interpretVertex(tokens, startingIndex + index * vertexColors.numTokensPerVertex(), vertexColors);
		}
		return vertices;
	}
	public VertexColors verticesAreColored(String[] tokens, int numVertices) {
		return hasColoredVertices(tokens, numVertices) ? VertexColors.COLORED :
				VertexColors.UNCOLORED;
	}
	public boolean hasColoredVertices(String[] tokens, int numVertices) {
		return tokens.length == numTokensForCommandWithNVertices(numVertices);
	}
	public int numTokensForCommandWithNVertices(int numVertices) {
		return NUM_TOKENS_FOR_COMMAND + numVertices*(NUM_TOKENS_FOR_COLORED_VERTEX);
	}

	private Vertex3D interpretVertex(Vertex3D v) {
		Vertex3D new_point = v;
		Point3DH Point3DH = transformToView(new_point);
		Color color = defaultColor;
		double z_camera = Point3DH.getZ();
		if(z_camera >= z_near) {
			color = ambientLight.multiply(ObjectColor);
		}
		else if (z_camera <= z_far) {
			color = depthColor;
		}else if(z_camera <= z_near && z_camera >= z_far) {
			Color lightingColor = ambientLight.multiply(ObjectColor);
			double r_slope = (depthColor.getR() - lightingColor.getR()) / (z_far - z_near);
			double g_slope = (depthColor.getG() - lightingColor.getG()) / (z_far - z_near);
			double b_slope = (depthColor.getB() - lightingColor.getB()) / (z_far - z_near);
			double dZ = (z_camera) - (z_near);
			double vertex_r = lightingColor.getR() + dZ * r_slope;
			double vertex_g = lightingColor.getG() + dZ * g_slope;
			double vertex_b = lightingColor.getB() + dZ * b_slope;
			color = new Color(vertex_r, vertex_g, vertex_b);
		}

		Vertex3D final_point = new Vertex3D(Point3DH.getX(), Point3DH.getY(), Point3DH.getZ(), color);
		if(v.hasNormal) {
			final_point.setNormal(getNormalByCTM(v.Normal));
		}
		return final_point;

	}

	private Vertex3D interpretVertex(String[] tokens, int startingIndex, VertexColors colored) {
		Point3DH point = interpretPoint(tokens, startingIndex);


		Color color = defaultColor;
		if(colored == VertexColors.COLORED) {
			color = interpretColor(tokens, startingIndex + NUM_TOKENS_FOR_POINT);
		}

		// TODO: finish this method


		Vertex3D new_point = new Vertex3D(point, Color.WHITE);
		Point3DH Point3DH = transformToView(new_point);

		double z_camera = Point3DH.getZ();
		if(z_camera >= z_near) {
			color = ambientLight.multiply(ObjectColor);
		}
		else if (z_camera <= z_far) {
			color = depthColor;
	    }else if(z_camera <= z_near && z_camera >= z_far) {
			Color lightingColor = ambientLight.multiply(ObjectColor);
			double r_slope = (depthColor.getR() - lightingColor.getR()) / (z_far - z_near);
			double g_slope = (depthColor.getG() - lightingColor.getG()) / (z_far - z_near);
			double b_slope = (depthColor.getB() - lightingColor.getB()) / (z_far - z_near);
			double dZ = (z_camera) - (z_near);
			double vertex_r = lightingColor.getR() + dZ * r_slope;
			double vertex_g = lightingColor.getG() + dZ * g_slope;
			double vertex_b = lightingColor.getB() + dZ * b_slope;
			color = new Color(vertex_r, vertex_g, vertex_b);
		}

		Vertex3D final_point = new Vertex3D(Point3DH.getX(), Point3DH.getY(), Point3DH.getZ(), color);

		return final_point;




	}
	public static Point3DH interpretPoint(String[] tokens, int startingIndex) {
		double x = cleanNumber(tokens[startingIndex]);
		double y = cleanNumber(tokens[startingIndex + 1]);
		double z = cleanNumber(tokens[startingIndex + 2]);

		// TODO: finish this method
		//System.out.println("interpretRotate");

		double current_point[] = new double[4];
		double temp[] = new double[4];
		current_point[0] = x;
		current_point[1] = y;
		current_point[2] = z;
		current_point[3] = 1.0;


		for(int i = 0; i < 4; i++) {
			double value = 0;
			for(int j = 0; j < 4; j++) {
				//value += CTM.getMatrix()[i][j] * current_point[j];
				value += CTM[i][j] * current_point[j];
			}
			temp[i] = value;
		}
		current_point = temp;



		Point3DH CTM_point = new Point3DH(current_point[0], current_point[1], current_point[2], current_point[3]);

		return CTM_point;
	}
	public static Color interpretColor(String[] tokens, int startingIndex) {
		double r = cleanNumber(tokens[startingIndex]);
		double g = cleanNumber(tokens[startingIndex + 1]);
		double b = cleanNumber(tokens[startingIndex + 2]);

		// TODO: finish this method

		Color color =  Color.fromARGB(Color.makeARGB((int)Math.round(r), (int)Math.round(g), (int)Math.round(b)));
		return color;
	}


	private void line(Vertex3D p1, Vertex3D p2) {
		Vertex3D P1 = transformToCamera(p1);
		Vertex3D P2 = transformToCamera(p2);
		// TODO: finish this method
		lineRenderer.drawLine(P1, P2, depthCueingDrawable);
	}
	private void polygon(Vertex3D p1, Vertex3D p2, Vertex3D p3) {
		Vertex3D P1 = transformToCamera(p1);
		Vertex3D P2 = transformToCamera(p2);
		Vertex3D P3 = transformToCamera(p3);
		// TODO: finish this method
	}

	public Vertex3D transformToCamera(Vertex3D vertex) {
		// TODO: finish this method

		// Assignment 2s :
		Point3DH p3d = vertex.getPoint3D();
		//------------------------------------------------------

		double point[] = new double[4];
		double temp[] = new double[4];
		double ratio = 1;
		if (has_camera){
			ratio = 200 / Math.max(x_high - x_low , y_high - y_low);
		}
		point[0] = p3d.getX();
		point[1] = p3d.getY();
		point[2] = p3d.getZ();
		point[3] = p3d.getW();
		if (x_high + x_low != 0) {
			double move;
			move = -(x_high + x_low) / 2;
			point[0] += move;
		}
		if (y_high + y_low != 0) {
			double move;
			move = -(y_high + y_low) / 2;
			point[1] += move;
		}
		point[0] *= ratio;
		point[1] *= ratio;


		for(int i = 0; i < 4; i++) {
			double value = 0;
			for(int j = 0; j < 4; j++) {
				value += toworld[i][j] * point[j];
			}
			temp[i] = value;
		}
		point = temp;
		Point3DH new_point = new Point3DH(point[0], point[1], point[2],point[3]);
		//System.out.println("The points after window matrix: (" + new_point.getX() + ", " +
		//new_point.getY() + ", " + new_point.getZ() + ")");
		return new Vertex3D(new_point,vertex.getColor());
	}

	// Assignment 3:


	private Vertex3D[] transformToViewS(Vertex3D[] vertices) {
		int size = vertices.length;
		Vertex3D[] new_vertices = new Vertex3D[size];
		for (int i =0; i<size; i++){
			new_vertices[i] = new Vertex3D(transformToView(vertices[i]),vertices[i].getColor());
		}
		return new_vertices;
	}


	private Point3DH transformToView(Vertex3D vertex) {
		// TODO: Assignment 3:

		Point3DH p3d = vertex.getPoint3D();
		if (has_camera) {

			double point[] = new double[4];
			double temp[] = new double[4];
			point[0] = p3d.getX();
			point[1] = p3d.getY();
			point[2] = p3d.getZ();
			point[3] = p3d.getW();


			for (int i = 0; i < 4; i++) {
				double value = 0;
				for (int j = 0; j < 4; j++) {
					value += inverseCamera[i][j] * point[j];
				}
				temp[i] = value;
			}
			point = temp;

			//point[0] /= -point[2];
			//point[1] /= -point[2];
			Point3DH new_point = new Point3DH(point[0], point[1], point[2]).euclidean();
			//System.out.println("The points after window matrix: (" + new_point.getX() + ", " +  new_point.getY() + ", " + new_point.getZ() + ")");
			return new_point;
		}else{
			return p3d;
		}
	}

	private ArrayList<Vertex3D> transformTo_Z_deepthS(ArrayList<Vertex3D> vertices) {
		int size = vertices.size();
		ArrayList<Vertex3D> new_vertices = new ArrayList<Vertex3D>();
		for (int i =0; i<size; i++){
			Vertex3D newV = new Vertex3D(transformTo_Z_deepth(vertices.get(i)),vertices.get(i).getColor());
			newV.setCmaeraPoint(vertices.get(i).getX(), vertices.get(i).getY(), vertices.get(i).getZ());
			new_vertices.add(newV);
		}
		return new_vertices;
	}

	private Point3DH transformTo_Z_deepth(Vertex3D vertex) {
		Point3DH p3d = vertex.getPoint3D();
		if (has_camera) {
			double point[] = new double[4];
			point[0] = p3d.getX();
			point[1] = p3d.getY();
			point[2] = p3d.getZ();
			point[3] = p3d.getW();
			double d = -1;
			if ( point[2] == 0){
				point[3] = 0;
			}
			double k = point[2]/d;
			//point[3] *= k;
			point[0] /= k;
			point[1] /= k;
			//point[2] =1;
			Point3DH new_point = new Point3DH(point[0], point[1], point[2], point[3]);
			return new_point;
		}else{
			return p3d;
		}
	}

	private ArrayList<Vertex3D> cut_vertices_zFar_and_near(ArrayList<Vertex3D> vertices) {
		int size = vertices.size();
		ArrayList<Vertex3D> new_vertices = new ArrayList<Vertex3D>();
		for (int i =0; i<size; i++){
			new_vertices.add(cut_zFar_and_near(vertices.get(i)));
		}
		return new_vertices;
	}

	private Vertex3D[] cut_vertices_zFar_and_near(Vertex3D[] vertices) {
		int size = vertices.length;
		Vertex3D[] new_vertices = new Vertex3D[size];
		for (int i =0; i<size; i++){
			new_vertices[i] = cut_zFar_and_near(vertices[i]);
		}
		return new_vertices;
	}

	private Vertex3D cut_zFar_and_near (Vertex3D point){
		Color color = point.getColor();
		double z_camera = point.getZ();
		if(z_camera >= z_near) {
			color = ambientLight.multiply(ObjectColor);
		}
		else if (z_camera <= z_far) {
			color = depthColor;
		}else if(z_camera <= z_near && z_camera >= z_far) {
			Color lightingColor = ambientLight.multiply(ObjectColor);
			double r_slope = (depthColor.getR() - lightingColor.getR()) / (z_far - z_near);
			double g_slope = (depthColor.getG() - lightingColor.getG()) / (z_far - z_near);
			double b_slope = (depthColor.getB() - lightingColor.getB()) / (z_far - z_near);
			double dZ = (z_camera) - (z_near);
			double vertex_r = lightingColor.getR() + dZ * r_slope;
			double vertex_g = lightingColor.getG() + dZ * g_slope;
			double vertex_b = lightingColor.getB() + dZ * b_slope;
			color = new Color(vertex_r, vertex_g, vertex_b);
		}
		return new Vertex3D(point.getX(), point.getY(), point.getZ(), color);
	}

	public static Point3DH interpretPointWithW(String[] tokens, int startingIndex) {
		double x = cleanNumber(tokens[startingIndex]);
		double y = cleanNumber(tokens[startingIndex + 1]);
		double z = cleanNumber(tokens[startingIndex + 2]);
		double w = cleanNumber(tokens[startingIndex + 3]);
		Point3DH point = new Point3DH(x, y, z, w);
		return point;
	}

	private void objFile(String filename) {
		ObjReader objReader = new ObjReader(filename, defaultColor, lightList, ShadingStyle, SpecularCoefficient, SpecularExp, ObjectColor);
		objReader.read();
		objReader.render(this);
	}


	public double[][] get_inverse(double[][] matrix) {
		double determinant = matrix[0][0] * matrix[1][1] * matrix[2][2] * matrix[3][3] + matrix[0][0] * matrix[1][2] * matrix[2][3] * matrix[3][1] +
				matrix[0][0] * matrix[1][3] * matrix[2][1] * matrix[3][2] + matrix[0][1] * matrix[1][0] * matrix[2][3] * matrix[3][2] +
				matrix[0][1] * matrix[1][2] * matrix[2][0] * matrix[3][3] + matrix[0][1] * matrix[1][3] * matrix[2][2] * matrix[3][0] +
				matrix[0][2] * matrix[1][0] * matrix[2][1] * matrix[3][3] + matrix[0][2] * matrix[1][1] * matrix[2][3] * matrix[3][0] +
				matrix[0][2] * matrix[1][3] * matrix[2][0] * matrix[3][1] + matrix[0][3] * matrix[1][0] * matrix[2][2] * matrix[3][1] +
				matrix[0][3] * matrix[1][1] * matrix[2][0] * matrix[3][2] + matrix[0][3] * matrix[1][2] * matrix[2][1] * matrix[3][0] -
				matrix[0][0] * matrix[1][1] * matrix[2][3] * matrix[3][2] - matrix[0][0] * matrix[1][2] * matrix[2][1] * matrix[3][3] -
				matrix[0][0] * matrix[1][3] * matrix[2][2] * matrix[3][1] - matrix[0][1] * matrix[1][0] * matrix[2][2] * matrix[3][3] -
				matrix[0][1] * matrix[1][2] * matrix[2][3] * matrix[3][0] - matrix[0][1] * matrix[1][3] * matrix[2][0] * matrix[3][2] -
				matrix[0][2] * matrix[1][0] * matrix[2][3] * matrix[3][1] - matrix[0][2] * matrix[1][1] * matrix[2][0] * matrix[3][3] -
				matrix[0][2] * matrix[1][3] * matrix[2][1] * matrix[3][0] - matrix[0][3] * matrix[1][0] * matrix[2][1] * matrix[3][2] -
				matrix[0][3] * matrix[1][1] * matrix[2][2] * matrix[3][0] - matrix[0][3] * matrix[1][2] * matrix[2][0] * matrix[3][1];
		double coefficient = 1/determinant;

		double [][] ret = new double[4][4];
		ret[0][0] = coefficient * (matrix[1][1] * matrix[2][2] * matrix[3][3] +
				matrix[1][2] * matrix[2][3] * matrix[3][1] +
				matrix[1][3] * matrix[2][1] * matrix[3][2] -
				matrix[1][1] * matrix[2][3] * matrix[3][2] -
				matrix[1][2] * matrix[2][1] * matrix[3][3] -
				matrix[1][3] * matrix[2][2] * matrix[3][1]);
		ret[0][1] = coefficient * (matrix[0][1] * matrix[2][3] * matrix[3][2] +
				matrix[0][2] * matrix[2][1] * matrix[3][3] +
				matrix[0][3] * matrix[2][2] * matrix[3][1] -
				matrix[0][1] * matrix[2][2] * matrix[3][3] -
				matrix[0][2] * matrix[2][3] * matrix[3][1] -
				matrix[0][3] * matrix[2][1] * matrix[3][2]);
		ret[0][2] = coefficient * (matrix[0][1] * matrix[1][2] * matrix[3][3] +
				matrix[0][2] * matrix[1][3] * matrix[3][1] +
				matrix[0][3] * matrix[1][1] * matrix[3][2] -
				matrix[0][1] * matrix[1][3] * matrix[3][2] -
				matrix[0][2] * matrix[1][1] * matrix[3][3] -
				matrix[0][3] * matrix[1][2] * matrix[3][1]);
		ret[0][3] = coefficient * (matrix[0][1] * matrix[1][3] * matrix[2][2] +
				matrix[0][2] * matrix[1][1] * matrix[2][3] +
				matrix[0][3] * matrix[1][2] * matrix[2][1] -
				matrix[0][1] * matrix[1][2] * matrix[2][3] -
				matrix[0][2] * matrix[1][3] * matrix[2][1] -
				matrix[0][3] * matrix[1][1] * matrix[2][2]);
		ret[1][0] = coefficient * (matrix[1][0] * matrix[2][3] * matrix[3][2] +
				matrix[1][2] * matrix[2][0] * matrix[3][3] +
				matrix[1][3] * matrix[2][2] * matrix[3][0] -
				matrix[1][0] * matrix[2][2] * matrix[3][3] -
				matrix[1][2] * matrix[2][3] * matrix[3][0] -
				matrix[1][3] * matrix[2][0] * matrix[3][2]);
		ret[1][1] = coefficient * (matrix[0][0] * matrix[2][2] * matrix[3][3] +
				matrix[0][2] * matrix[2][3] * matrix[3][0] +
				matrix[0][3] * matrix[2][0] * matrix[3][2] -
				matrix[0][0] * matrix[2][3] * matrix[3][2] -
				matrix[0][2] * matrix[2][0] * matrix[3][3] -
				matrix[0][3] * matrix[2][2] * matrix[3][0]);
		ret[1][2] = coefficient * (matrix[0][0] * matrix[1][3] * matrix[3][2] +
				matrix[0][2] * matrix[1][0] * matrix[3][3] +
				matrix[0][3] * matrix[1][2] * matrix[3][0] -
				matrix[0][0] * matrix[1][2] * matrix[3][3] -
				matrix[0][2] * matrix[1][3] * matrix[3][0] -
				matrix[0][3] * matrix[1][0] * matrix[3][2]);
		ret[1][3] = coefficient * (matrix[0][0] * matrix[1][2] * matrix[2][3] +
				matrix[0][2] * matrix[1][3] * matrix[2][0] +
				matrix[0][3] * matrix[1][0] * matrix[2][2] -
				matrix[0][0] * matrix[1][3] * matrix[2][2] -
				matrix[0][2] * matrix[1][0] * matrix[2][3] -
				matrix[0][3] * matrix[1][2] * matrix[2][0]);
		ret[2][0] = coefficient * (matrix[1][0] * matrix[2][1] * matrix[3][3] +
				matrix[1][1] * matrix[2][3] * matrix[3][0] +
				matrix[1][3] * matrix[2][0] * matrix[3][1] -
				matrix[1][0] * matrix[2][3] * matrix[3][1] -
				matrix[1][1] * matrix[2][0] * matrix[3][3] -
				matrix[1][3] * matrix[2][1] * matrix[3][0]);
		ret[2][1] = coefficient * (matrix[0][0] * matrix[2][3] * matrix[3][1] +
				matrix[0][1] * matrix[2][0] * matrix[3][3] +
				matrix[0][3] * matrix[2][1] * matrix[3][0] -
				matrix[0][0] * matrix[2][1] * matrix[3][3] -
				matrix[0][1] * matrix[2][3] * matrix[3][0] -
				matrix[0][3] * matrix[2][0] * matrix[3][1]);
		ret[2][2] = coefficient * (matrix[0][0] * matrix[1][1] * matrix[3][3] +
				matrix[0][1] * matrix[1][3] * matrix[3][0] +
				matrix[0][3] * matrix[1][0] * matrix[3][1] -
				matrix[0][0] * matrix[1][3] * matrix[3][1] -
				matrix[0][1] * matrix[1][0] * matrix[3][3] -
				matrix[0][3] * matrix[1][1] * matrix[3][0]);
		ret[2][3] = coefficient * (matrix[0][0] * matrix[1][3] * matrix[2][1] +
				matrix[0][1] * matrix[1][0] * matrix[2][3] +
				matrix[0][3] * matrix[1][1] * matrix[2][0] -
				matrix[0][0] * matrix[1][1] * matrix[2][3] -
				matrix[0][1] * matrix[1][3] * matrix[2][0] -
				matrix[0][3] * matrix[1][0] * matrix[2][1]);
		ret[3][0] = coefficient * (matrix[1][0] * matrix[2][2] * matrix[3][1] +
				matrix[1][1] * matrix[2][0] * matrix[3][2] +
				matrix[1][2] * matrix[2][1] * matrix[3][0] -
				matrix[1][0] * matrix[2][1] * matrix[3][2] -
				matrix[1][1] * matrix[2][2] * matrix[3][0] -
				matrix[1][2] * matrix[2][0] * matrix[3][1]);
		ret[3][1] = coefficient * (matrix[0][0] * matrix[2][1] * matrix[3][2] +
				matrix[0][1] * matrix[2][2] * matrix[3][0] +
				matrix[0][2] * matrix[2][0] * matrix[3][1] -
				matrix[0][0] * matrix[2][2] * matrix[3][1] -
				matrix[0][1] * matrix[2][0] * matrix[3][2] -
				matrix[0][2] * matrix[2][1] * matrix[3][0]);
		ret[3][2] = coefficient * (matrix[0][0] * matrix[1][2] * matrix[3][1] +
				matrix[0][1] * matrix[1][0] * matrix[3][2] +
				matrix[0][2] * matrix[1][1] * matrix[3][0] -
				matrix[0][0] * matrix[1][1] * matrix[3][2] -
				matrix[0][1] * matrix[1][2] * matrix[3][0] -
				matrix[0][2] * matrix[1][0] * matrix[3][1]);
		ret[3][3] = coefficient * (matrix[0][0] * matrix[1][1] * matrix[2][2] +
				matrix[0][1] * matrix[1][2] * matrix[2][0] +
				matrix[0][2] * matrix[1][0] * matrix[2][1] -
				matrix[0][0] * matrix[1][2] * matrix[2][1] -
				matrix[0][1] * matrix[1][0] * matrix[2][2] -
				matrix[0][2] * matrix[1][1] * matrix[2][0]);
	return ret;
	}


	private void interpretCamera(String[] tokens) {
		x_low = cleanNumber(tokens[1]);
		y_low = cleanNumber(tokens[2]);
		x_high = cleanNumber(tokens[3]);
		y_high = cleanNumber(tokens[4]);
		hither = cleanNumber(tokens[5]);
		yon = cleanNumber(tokens[6]);
		inverseCamera = get_inverse(CTM);
		has_camera = true;

//		double deltaY = y_high - y_low;
//		double deltaX = x_high - x_low;
//		double larger_distance = deltaX >= deltaY ? deltaX : deltaY;
//		double dX_dY_ratio = deltaX / deltaY;
//		double height_ratio = drawable.getDimensions().getHeight() / larger_distance;
//		double width_ratio = drawable.getDimensions().getWidth() / larger_distance;
//
//		//we need to cut the things if it is not square
//		// this is look like :
//		//     *********
//		//     *********
//		//     *********
//		// since dx is larger than dy:
//		if(dX_dY_ratio >= 1) {
//			camera_height = ((int)Math.round(drawable.getDimensions().getHeight() / dX_dY_ratio));
//		}
//		//    ***
//		//    ***
//		//    ***
//		//    dx is smaller than dy
//		else {
//			camera_width = ((int)Math.round(drawable.getDimensions().getWidth() * dX_dY_ratio));
//		}
//		//System.out.println("haha");
//		double bottom;
//		double right;
//		if(camera_height >= camera_width) {
//			right = (650 - camera_width) / 2;
//		}
//		else {
//			bottom = (650 - camera_height) / 2;
//		}
	}

	private void interpretSurface(String[] tokens) {
		double r = cleanNumber(tokens[1]);
		double g = cleanNumber(tokens[2]);
		double b = cleanNumber(tokens[3]);
		defaultColor = new Color(r,g,b);
		surfaceColor = defaultColor;
		// assignment4 add:
		ObjectColor = surfaceColor;
		SpecularCoefficient = cleanNumber(tokens[4]);
		SpecularExp = cleanNumber(tokens[5]);
	}

	private void interpretPhong() {
		PhongShading = true;
		ShadingStyle = "phong";
	}
	// interpret gouraud
	private void interpretGouraud() {
		GouraudShading = true;
		ShadingStyle = "gouraud";
	}

	private void interpretFlat() {
		FlatShading = true;
		ShadingStyle = "flat";
	}
	// interpret light source
	private void interpretLight(String[] tokens) {
		double light_r = cleanNumber(tokens[1]);
		double light_g = cleanNumber(tokens[2]);
		double light_b = cleanNumber(tokens[3]);
		// form light color (light intensity)
		LightIntensity = new Color(light_r, light_g, light_b);
		// get attenuation constants
		attenuationA = cleanNumber(tokens[4]);
		attenuationB = cleanNumber(tokens[5]);
		// get the location of light source
		LightLocation = new Vertex3D(0, 0, 0, LightIntensity);
		Vertex3D lightCTM = getLightlocationByCTM(LightLocation);
		Point3DH lightInView = transformToView(lightCTM);
		// the location of light source
		LightLocation = new Vertex3D(lightInView.getX(), lightInView.getY(), lightInView.getZ(), LightLocation.getColor());
		lightBulb = new Light(LightIntensity, attenuationA, attenuationB, LightLocation);
		// add lightBulb into light arrayList
		lightList.add(lightBulb);
	}

	private Vertex3D getLightlocationByCTM(Vertex3D location) {

		double current_point[] = new double[4];
		double temp[] = new double[4];
		current_point[0] = location.getX();
		current_point[1] = location.getY();
		current_point[2] = location.getZ();
		current_point[3] = 1.0;		// w in homogeneous point

		for(int i = 0; i < 4; i++) {
			double value = 0;
			for(int j = 0; j < 4; j++) {
				value += CTM[i][j] * current_point[j];
			}
			temp[i] = value;
		}
		current_point = temp;

		Point3DH CTM_point = new Point3DH(current_point[0], current_point[1], current_point[2]);
		Vertex3D ret = new Vertex3D(CTM_point, Color.WHITE);
		return ret;
	}

	private void interpretAmbient(String[] tokens) {
		double r = cleanNumber(tokens[1]);
		double g = cleanNumber(tokens[2]);
		double b = cleanNumber(tokens[3]);
		ambientLight = new Color(r,g,b);
		defaultColor = ambientLight;
	}

	private void interpretDepth(String[] tokens) {
		z_near = cleanNumber(tokens[1]);
		z_far = cleanNumber(tokens[2]);
		double depth_r = cleanNumber(tokens[3]);
		double depth_g = cleanNumber(tokens[4]);
		double depth_b = cleanNumber(tokens[5]);
		depthColor = new Color(depth_r, depth_g, depth_b);
	}

	private void interpretObj(String[] tokens) {
		String str= tokens[1].replace("\"", "");
		String objFilename = str + ".obj";
		objFile(objFilename);
	}


}
