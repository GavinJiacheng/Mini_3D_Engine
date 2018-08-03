package client.interpreter;

import geometry.Point3DH;
import geometry.Vertex3D;
import polygon.Polygon;
import shading.Light;
import windowing.graphics.Color;

import java.util.ArrayList;
import java.util.List;

class ObjReader {
	private static final char COMMENT_CHAR = '#';
	private static final int NOT_SPECIFIED = -1;

	private class ObjVertex {
		// TODO: fill this class in.  Store indices for a vertex, a texture, and a normal.  Have getters for them.

		int	vertexIndex = -1;
		int textureIndex= -1;
		int normalIndex= -1;

		private ObjVertex(int vertexIndex, int textureIndex, int normalIndex){
			this.vertexIndex = vertexIndex;
			this.textureIndex = textureIndex;
			this.normalIndex = normalIndex;
		}
		public int getVertex(){return vertexIndex;}
		public int getNormalIndex(){return normalIndex;}

	}
	private class ObjFace extends ArrayList<ObjVertex> {
		private static final long serialVersionUID = -4130668677651098160L;
	}
	private LineBasedReader reader;

	private List<Vertex3D> objVertices;
	private List<Vertex3D> transformedVertices;
	private List<Point3DH> objNormals;
	private List<ObjFace> objFaces;

	private Color defaultColor;

	private ArrayList<Light> Lights;

	private List<Vertex3D> ShadedPoints;
	private String ShadingStyle;
	private double SpecularCoefficient;
	private double SpecularExp;
	private Color ObjectColor;


	public ObjReader(String filename, Color defaultColor, ArrayList<Light> Lights, String ShadingStyle, double SpecularCoefficient, double SpecularExp, Color ObjectColor) {
		// TODO: Initialize an instance of this class.
		ShadedPoints = new ArrayList<Vertex3D>();
		this.Lights = Lights;
		this.ShadingStyle = ShadingStyle;
		this.SpecularCoefficient = SpecularCoefficient;
		this.SpecularExp = SpecularExp;
		this.ObjectColor = ObjectColor;

		objVertices = new ArrayList<Vertex3D>();
		transformedVertices = new ArrayList<Vertex3D>();
		objNormals = new ArrayList<Point3DH>();
		objVertices = new ArrayList<>();
		reader = new LineBasedReader(filename);
		objFaces = new ArrayList<ObjFace>();
		this.defaultColor = defaultColor;
	}

	public void render(SimpInterpreter interpreter) {
		// TODO: Implement.  All of the vertices, normals, and faces have been defined.
		// First, transform all of the vertices.
		// Then, go through each face, break into triangles if necessary, and send each triangle to the renderer.
		// You may need to add arguments to this function, and/or change the visibility of functions in SimpInterpreter.


		for(ObjFace face : objFaces) {
			for(int j = 0; j < face.size() - 1; j ++) {
				int first_index = face.get(0).getVertex();
				int second_index = face.get(j).getVertex();
				int third_index = face.get(j + 1).getVertex();

				int first_normal = face.get(0).getNormalIndex();
				int second_normal = face.get(j).getNormalIndex();
				int third_normal = face.get(j + 1).getNormalIndex();

				Vertex3D V1 = objVertices.get(first_index);
				Vertex3D V2 = objVertices.get(second_index);
				Vertex3D V3 = objVertices.get(third_index);

				if (first_normal >= 0){
					V1.setNormal(objNormals.get(first_normal));
				}else if(second_normal >= 0){
					V2.setNormal(objNormals.get(second_normal));
				}else if(third_normal >= 0){
					V3.setNormal(objNormals.get(third_normal));
				}


				Polygon polygon_face = Polygon.make(V1, V2, V3);
				interpreter.interpretPolygonFromObj(V1, V2, V3);
			}
		}
	}

//	private Polygon polygonForFace(ObjFace face) {
//		// TODO: This function might be used in render() above.  Implement it if you find it handy.
//	}

	public void read() {
		while(reader.hasNext() ) {
			String line = reader.next().trim();
			interpretObjLine(line);
		}
	}
	private void interpretObjLine(String line) {
		if(!line.isEmpty() && line.charAt(0) != COMMENT_CHAR) {
			String[] tokens = line.split("[ \t,()]+");
			if(tokens.length != 0) {
				interpretObjCommand(tokens);
			}
		}
	}

	private void interpretObjCommand(String[] tokens) {
		switch(tokens[0]) {
		case "v" :
		case "V" :
			interpretObjVertex(tokens);
			break;
		case "vn":
		case "VN":
			interpretObjNormal(tokens);
			break;
		case "f":
		case "F":
			interpretObjFace(tokens);
			break;
		default:	// do nothing
			break;
		}
	}
	private void interpretObjFace(String[] tokens) {
		ObjFace face = new ObjFace();

		for(int i = 1; i<tokens.length; i++) {
			String token = tokens[i];
			String[] subtokens = token.split("/");

			int vertexIndex  = objIndex(subtokens, 0, objVertices.size());
			int textureIndex = objIndex(subtokens, 1, 0);
			int normalIndex  = objIndex(subtokens, 2, objNormals.size());

			ObjVertex face_vertex = new ObjVertex(vertexIndex, textureIndex, normalIndex);
			face.add(face_vertex);
		}
		objFaces.add(face);
	}

	private int objIndex(String[] subtokens, int tokenIndex, int baseForNegativeIndices) {
		// TODO: write this.  subtokens[tokenIndex], if it exists, holds a string for an index.
		// use Integer.parseInt() to get the integer value of the index.
		// Be sure to handle both positive and negative indices.

        int index = 0;
        if (tokenIndex < 0 || tokenIndex >= subtokens.length){
			return NOT_SPECIFIED;
		}
		if (subtokens[tokenIndex].isEmpty()){
			return NOT_SPECIFIED;
		}
        if(!subtokens[tokenIndex].equals(null)) {
            index = Integer.parseInt(subtokens[tokenIndex]) - 1;
            if(index < 0) {
                index = baseForNegativeIndices - 1 - (Math.abs(index) - 1);
            }
        }
        return index;
	}

	private void interpretObjNormal(String[] tokens) {
		int numArgs = tokens.length - 1;
		if(numArgs != 3) {
			throw new BadObjFileException("vertex normal with wrong number of arguments : " + numArgs + ": " + tokens);
		}
		Point3DH normal = SimpInterpreter.interpretPoint(tokens, 1);
		// TODO: fill in action to take here.
		objNormals.add(normal);
	}
	private void interpretObjVertex(String[] tokens) {
		int numArgs = tokens.length - 1;
		Point3DH point = objVertexPoint(tokens, numArgs);
		Color color = objVertexColor(tokens, numArgs);

		// TODO: fill in action to take here.

		Vertex3D new_vertex = new Vertex3D(point, color);
		objVertices.add(new_vertex);

	}

	private Color objVertexColor(String[] tokens, int numArgs) {
		if(numArgs == 6) {
			return SimpInterpreter.interpretColor(tokens, 4);
		}
		if(numArgs == 7) {
			return SimpInterpreter.interpretColor(tokens, 5);
		}
		return defaultColor;
	}

	private Point3DH objVertexPoint(String[] tokens, int numArgs) {
		if(numArgs == 3 || numArgs == 6) {
			return SimpInterpreter.interpretPoint(tokens, 1);
		}
		else if(numArgs == 4 || numArgs == 7) {
			return SimpInterpreter.interpretPointWithW(tokens, 1);
		}
		throw new BadObjFileException("vertex with wrong number of arguments : " + numArgs + ": " + tokens);
	}
}