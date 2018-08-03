




package client.interpreter;


import java.util.ArrayList;
import java.util.Stack;

import client.interpreter.LineBasedReader;
import geometry.*;
import line.LineRenderer;
import client.Clipper;
import client.DepthCueingDrawable;
import client.RendererTrio;
import polygon.Polygon;
import polygon.PolygonRenderer;
import polygon.Shader;
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

	private Transformation CTM;
	// If I make CTM as a Transformation, its number will be very larege, I don't know why. I have to make Transformation as static
	// and make CTM as an array. Now it works.
	// this problem is fixed.
	//private static double CTM[][] = new double [4][4];
	private Stack<Transformation> CTMStack = new Stack<Transformation>();
	private Transformation worldToScreen;

	private static int WORLD_LOW_X = -100;
	private static int WORLD_HIGH_X = 100;
	private static int WORLD_LOW_Y = -100;
	private static int WORLD_HIGH_Y = 100;

	private LineBasedReader reader;
	private Stack<LineBasedReader> readerStack;

	private Color defaultColor = Color.WHITE;
	private Color ambientLight = Color.BLACK;

	private Drawable drawable;
	private Drawable depthCueingDrawable;

	private LineRenderer lineRenderer;
	private PolygonRenderer filledRenderer;
	private PolygonRenderer wireframeRenderer;
	private Transformation cameraToScreen;
	private Clipper clipper;

	// add things here

	public static double CTM_ForWire[][] = Transformation.identity().getMatrix();
	public static boolean NoWire = true;
	public static boolean backSide = false;
	private Color DarkColor = Color.WHITE;

	public enum RenderStyle {
		FILLED,
		WIREFRAME;
	}
	public SimpInterpreter(String filename,
			Drawable drawable,
			RendererTrio renderers) {
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
		CTM = Transformation.identity();
	}

	private double [][] toWindowSpace = Transformation.identity().getMatrix();

	private void makeWorldToScreenTransform(Dimensions dimensions) {
		// TODO: fill this in

		double height = dimensions.getHeight();
		double width = dimensions.getWidth();

		double WorldH = height / (WORLD_HIGH_Y - WORLD_LOW_Y);
		double WorldW = width / (WORLD_HIGH_X - WORLD_LOW_X);

		double translate_height = 100;
		double translate_width = 100;

		toWindowSpace[0][3] = translate_width;
		toWindowSpace[1][3] = translate_height;
		//toWindowSpace[2][3] = translate_height;
		//for tran Z

		double temp[][];
		temp = Transformation.identity().getMatrix();
		temp[0][0] = WorldH;
		temp[1][1] = WorldW;

		double NewArray[][] = Transformation.identity().getMatrix();

		double value;
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				value = 0;
				for(int k = 0; k < 4; k++) {
					value += temp[i][k] * toWindowSpace[k][j] ;
				}
				NewArray[i][j] = value;
				//System.out.println("df: " + NewArray[i][j]);
			}
		}
		toWindowSpace = NewArray;
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
//		case "camera" :		interpretCamera(tokens);	break;
//		case "surface" :	interpretSurface(tokens);	break;
//		case "ambient" :	interpretAmbient(tokens);	break;
//		case "depth" :		interpretDepth(tokens);		break;
//		case "obj" :		interpretObj(tokens);		break;

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
		CTM.scale(sx,sy,sz);


		/*
		Transformation trans = Transformation.identity();
		double tempCTM[][] = new double[4][4];
		trans.transformMatrix(0, 0, sx);
		trans.transformMatrix(1, 1, sy);
		trans.transformMatrix(2, 2, sz);

		// multiply transformation matrix with CTM
		double value;
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				value = 0;
				for(int k = 0; k < 4; k++) {
					value += CTM[i][k] * trans.getMatrix()[k][j] ;
				}
				tempCTM[i][j] = value;
			}
		}
		CTM = tempCTM;*/
	}
	private void interpretTranslate(String[] tokens) {
		double tx = cleanNumber(tokens[1]);
		double ty = cleanNumber(tokens[2]);
		double tz = cleanNumber(tokens[3]);
		//System.out.println("interpretTranslate");
		// TODO: finish this method
		// Problem---------------------------------------------------------------
		// translate matrix
		/*
		Transformation trans = Transformation.identity();
		double tempCTM[][] = new double[4][4];
		trans.transformMatrix(0, 3, tx);
		trans.transformMatrix(1, 3, ty);
		trans.transformMatrix(2, 3, tz);

		double value;
		double ctm[][] = CTM.getMatrix();
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				value = 0;
				for(int k = 0; k < 4; k++) {
					value += ctm[i][k] * trans.getMatrix()[k][j] ;
				}
				tempCTM[i][j] = value;
			}
		}
		*/
		CTM.translate(tx,ty,tz);
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
			trans.transformMatrix(1, 1, Math.cos(Math.toRadians(angleInDegrees)));
			trans.transformMatrix(2, 1, Math.sin(Math.toRadians(angleInDegrees)));
			trans.transformMatrix(1, 2, (-1) * Math.sin(Math.toRadians(angleInDegrees)));
			trans.transformMatrix(2, 2, Math.cos(Math.toRadians(angleInDegrees)));
		}
		else if(axisString.equals("Y")) {
			trans.transformMatrix(0, 0, Math.cos(Math.toRadians(angleInDegrees)));
			trans.transformMatrix(0, 2, Math.sin(Math.toRadians(angleInDegrees)));
			trans.transformMatrix(2, 0, (-1) * Math.sin(Math.toRadians(angleInDegrees)));
			trans.transformMatrix(2, 2, Math.cos(Math.toRadians(angleInDegrees)));
		}
		else if(axisString.equals("Z")) {
			trans.transformMatrix(0, 0, Math.cos(Math.toRadians(angleInDegrees)));
			trans.transformMatrix(0, 1, (-1) * Math.sin(Math.toRadians(angleInDegrees)));
			trans.transformMatrix(1, 0, Math.sin(Math.toRadians(angleInDegrees)));
			trans.transformMatrix(1, 1, Math.cos(Math.toRadians(angleInDegrees)));
		}

		double value;
		double [][] temp = CTM.getMatrix();
		double [][] TransArray = trans.getMatrix();
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				value = 0;
				for(int k = 0; k < 4; k++) {
					value += TransArray[i][k] * temp[k][j] ;
				}
				tempCTM[i][j] = value;
			}
		}
		CTM.setValue(tempCTM);
		//System.out.println("interpretRotate2");
	}
	private double cleanNumber(String string) {
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
		line(vertices[0],vertices[1]);
	}
	private void interpretPolygon(String[] tokens) {
		Vertex3D[] vertices = interpretVertices(tokens, 3, 1);

		// TODO: finish this method

		polygon(vertices[0], vertices[1], vertices[2]);

		Clipper clipper = new Clipper(-100, -100, 100, 100, vertices[0], vertices[1], vertices[2]);
		ArrayList<Vertex3D> points = clipper.NewPolygonPoints;
		//System.out.println(points.size());
		ArrayList<Vertex3D> Large_points = new ArrayList<Vertex3D>();
		for (Vertex3D visitor:points){
			Vertex3D new_point = new Vertex3D(visitor.getX()+200, visitor.getY()+200,visitor.getZ(),visitor.getColor());
			System.out.println("x: " + visitor.getX() + "y: "+visitor.getY());
			Large_points.add(new_point);
		}
		//System.out.println("MaxX is : "+maxX+"MaxY is :  "+maxY+" MinY is :  "+minY+"MinX is  :"+minX);
		int numberI = 1;
		int arraySize = Large_points.size() - 2;
		while(numberI <= arraySize) {
			Polygon triangle;
			triangle = Polygon.make(Large_points.get(numberI), Large_points.get(0), Large_points.get(numberI + 1));
			if (NoWire == true) {
				filledRenderer.drawPolygon(triangle, drawable);
			} else {
				wireframeRenderer.drawPolygon(triangle, drawable);
			}
			numberI++;
		}
		/*
		Clipper clipper = new Clipper(-100, -100, 100, 100, vertices[0], vertices[1], vertices[2]);
		ArrayList<Vertex3D> points = clipper.NewPolygonPoints;
		//System.out.println(points.size());
		ArrayList<Vertex3D> Large_points = new ArrayList<Vertex3D>();
		for (Vertex3D visitor:points){
			Vertex3D new_point = new Vertex3D(visitor.getX()*3+300, visitor.getY()*3+300,visitor.getZ(),visitor.getColor());
			Large_points.add(new_point);
		}
		//System.out.println("MaxX is : "+maxX+"MaxY is :  "+maxY+" MinY is :  "+minY+"MinX is  :"+minX);
		int numberI = 1;
		int arraySize = Large_points.size() - 2;
		while(numberI <= arraySize) {
			Polygon triangle;
			triangle = Polygon.make(Large_points.get(numberI), Large_points.get(0), Large_points.get(numberI + 1));
			if (NoWire == true) {
				filledRenderer.drawPolygon(triangle, drawable);
			} else {
				wireframeRenderer.drawPolygon(triangle, drawable);
			}
			numberI++;
		}
		*/

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


	private Vertex3D interpretVertex(String[] tokens, int startingIndex, VertexColors colored) {
		Point3DH point = interpretPoint(tokens, startingIndex);


		Color color = defaultColor;
		if(colored == VertexColors.COLORED) {
			color = interpretColor(tokens, startingIndex + NUM_TOKENS_FOR_POINT);
		}

		// TODO: finish this method

		Point3DH camera_center = transformToCamera(point);


		Vertex3D final_point = new Vertex3D(camera_center.getX(), camera_center.getY(), camera_center.getZ(), color);

		return final_point;

	}
	public Point3DH interpretPoint(String[] tokens, int startingIndex) {
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
				value += CTM.getMatrix()[i][j] * current_point[j];
			}
			temp[i] = value;
		}
		current_point = temp;



		Point3DH CTM_point = new Point3DH(current_point[0], current_point[1], current_point[2]);

		return CTM_point;
	}
	public Color interpretColor(String[] tokens, int startingIndex) {
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

	private Vertex3D transformToCamera(Vertex3D vertex) {
		// TODO: finish this method

		Point3DH p3d = vertex.getPoint3D();

		double point[] = new double[4];
		double temp[] = new double[4];
		point[0] = p3d.getX();
		point[1] = p3d.getY();
		point[2] = p3d.getZ();
		point[3] = p3d.getW();


		Point3DH new_point = new Point3DH(point[0], point[1], point[2]).euclidean();
		return new Vertex3D(new_point,vertex.getColor());
	}

	public Point3DH transformToCamera(Point3DH p3d) {

		double point[] = new double[4];
		point[0] = p3d.getX();
		point[1] = p3d.getY();
		point[2] = p3d.getZ();
		point[3] = p3d.getW();
		return new Point3DH(point[0], point[1], point[2]).euclidean();

	}



}
