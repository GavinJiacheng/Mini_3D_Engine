package client;

import client.testPages.*;
import geometry.Point2D;
import line.*;
import polygon.FilledPolygonRenderer;
import polygon.WireframePolygonRenderer;
import polygon.PolygonRenderer;
import windowing.PageTurner;
import windowing.drawable.*;
import windowing.graphics.Color;
import windowing.graphics.Dimensions;
// for assignment 2:
import client.interpreter.LineBasedReader;
import client.interpreter.SimpInterpreter;

public class Client implements PageTurner {
	private static final int ARGB_WHITE = 0xff_ff_ff_ff;
	private static final int ARGB_GREEN = 0xff_00_ff_40;


	private static final int NUM_PAGES = 16;
	protected static final double GHOST_COVERAGE = 0.14;

	private static final int NUM_PANELS = 4;
	private static final Dimensions PANEL_SIZE = new Dimensions(300, 300);
	private static final Point2D[] lowCornersOfPanels = {
			new Point2D( 50, 400),
			new Point2D(400, 400),
			new Point2D( 50,  50),
			new Point2D(400,  50),
	};
	
	private final Drawable drawable;
	private int pageNumber = 0;
	
	private Drawable image;
	private Drawable[] panels;
	private Drawable[] ghostPanels;					// use transparency and write only white
	private Drawable largePanel;
	private Drawable fullPanel;
	
	private LineRenderer lineRenderers[];
	private PolygonRenderer polygonRenderer;
	private PolygonRenderer wireframeRenderer;
	
	
	public Client(Drawable drawable) {
		this.drawable = drawable;
		createDrawables();
		createRenderers();
	}

	public void createDrawables() {
		image = new InvertedYDrawable(drawable);
		image = new TranslatingDrawable(image, point(0, 0), dimensions(750, 750));
		image = new ColoredDrawable(image, ARGB_WHITE);
		
		largePanel = new TranslatingDrawable(image, point(  50, 50),  dimensions(650, 650));
		fullPanel = new ThreeDTranslatingDrawable(image, point(  50, 50),  dimensions(650, 650));

		createPanels();
		createGhostPanels();
	}

	public void createPanels() {
		panels = new Drawable[NUM_PANELS];
		
		for(int index = 0; index < NUM_PANELS; index++) {
			panels[index] = new TranslatingDrawable(image, lowCornersOfPanels[index], PANEL_SIZE);
		}
	}

	private void createGhostPanels() {
		ghostPanels = new Drawable[NUM_PANELS];
		
		for(int index = 0; index < NUM_PANELS; index++) {
			Drawable drawable = panels[index];
			ghostPanels[index] = new GhostWritingDrawable(drawable, GHOST_COVERAGE);
		}
	}
	private Point2D point(int x, int y) {
		return new Point2D(x, y);
	}	
	private Dimensions dimensions(int x, int y) {
		return new Dimensions(x, y);
	}
	private void createRenderers() {
		
		lineRenderers = new LineRenderer[4];
 		lineRenderers[0] = BresenhamLineRenderer.make();
	//	lineRenderers[0] = ExpensiveLineRenderer.make();
		lineRenderers[1] = DDALineRenderer.make();
		lineRenderers[2] = AlternatingLineRenderer.make();
		//lineRenderers[3] = AlternatingLineRenderer.make();
		lineRenderers[3] = AntialiasingLineRenderer.make();

		wireframeRenderer = WireframePolygonRenderer.make();
		polygonRenderer = FilledPolygonRenderer.make();//wireframeRenderer;//FilledPolygonRenderer.make();
	}

	private SimpInterpreter interpreter;
	private String filename;
	private RendererTrio renderers;
	private boolean hasArgument;

	private void argumentNextPage() {
		image.clear();
		fullPanel.clear();

		interpreter = new SimpInterpreter(filename + ".simp", fullPanel, renderers);
		interpreter.interpret();
	}

	public void noArgumentNextPage() {
		System.out.println("PageNumber " + (pageNumber + 1));
		pageNumber = (pageNumber + 1) % NUM_PAGES;

		image.clear();
		fullPanel.clear();

		switch(pageNumber) {
			case 1:  filename = "page-a1";	 break;
			case 2:  filename = "page-a2";	 break;
			case 3:	 filename = "page-a3";	 break;
			case 4:  filename = "page-b1";	 break;
			case 5:  filename = "page-b2";	 break;
			case 6:  filename = "page-b3";	 break;
			case 7:  filename = "page-c1";	 break;
			case 8:  filename = "page-c2";	 break;
			case 9:  filename = "page-c3";	 break;
			case 10:  filename = "page-d";	 break;
			case 11:  filename = "page-e";	 break;
			case 12:  filename = "page-f1";	 break;
			case 13:  filename = "page-f2";	 break;
			case 14:  filename = "page-g";	 break;
			case 15:  filename = "page-h";	 break;
			case 0:  filename = "page-i";	 break;

			default: defaultPage();
				return;
		}
		interpreter = new SimpInterpreter(filename + ".simp", fullPanel, renderers);
		interpreter.interpret();
	}
	
	@Override
	public void nextPage() {

		renderers = new RendererTrio();
		if(hasArgument) {
			argumentNextPage();
		}
		else {
			noArgumentNextPage();
		}

//		// for assignment 2:
//
//		SimpInterpreter interpreter;
//
//		Drawable depthCueingDrawable;
//		System.out.println("PageNumber " + (pageNumber + 1));
//		pageNumber = (pageNumber + 1) % NUM_PAGES;
//
//		image.clear();
//		fullPanel.clear();
//
//		RendererTrio renderers = new RendererTrio();
//
//		switch(pageNumber) {
//			case 1:  new MeshPolygonTest(fullPanel, wireframeRenderer, MeshPolygonTest.USE_PERTURBATION);
//				break;
//			case 2: new MeshPolygonTest(fullPanel, polygonRenderer, MeshPolygonTest.USE_PERTURBATION);
//				break;
//			case 3: new centeredTriangleTest(fullPanel, polygonRenderer);
//				break;
//
//			case 4: System.out.println("F4");
//				depthCueingDrawable = new DepthCueingDrawable(fullPanel, 0, -200, Color.GREEN);
//				interpreter = new SimpInterpreter("tomsPage4.simp", depthCueingDrawable, renderers);
//				interpreter.interpret();
//				break;
//
//			case 5: System.out.println("F5");
//				depthCueingDrawable = new DepthCueingDrawable(fullPanel, 0, -200, Color.RED);
//				interpreter = new SimpInterpreter("tomsPage5.simp", depthCueingDrawable, renderers);
//				interpreter.interpret();
//				break;
//
//			case 6: System.out.println("F6");
//				depthCueingDrawable = new DepthCueingDrawable(fullPanel, 0, -200, Color.WHITE);
//				interpreter = new SimpInterpreter("page6.simp", depthCueingDrawable, renderers);
//				interpreter.interpret();
//				break;
//
//			case 7: System.out.println("F7");
//				depthCueingDrawable = new DepthCueingDrawable(fullPanel, 0, -200, Color.WHITE);
//				interpreter = new SimpInterpreter("page7.simp", depthCueingDrawable, renderers);
//				interpreter.interpret();
//				break;
//
//			case 0: System.out.println("F8");
//				depthCueingDrawable = new DepthCueingDrawable(fullPanel, 0, -200, Color.WHITE);
//				interpreter = new SimpInterpreter("page8.simp", depthCueingDrawable, renderers);
//				interpreter.interpret();
//				break;
//
//			default: defaultPage();
//				break;
//		}


		// for assignment 1:

		//System.out.println("PageNumber " + (pageNumber + 1));
		//pageNumber = (pageNumber + 1) % NUM_PAGES;
		
		//image.clear();
		//largePanel.clear();
		//switch(pageNumber) {
		//case 1:  lineDrawerPage((panel, renderer)->{ new StarburstLineTest(panel, renderer); });
		//		 break;
		//case 2:  lineDrawerPage((panel, renderer)->{ new ParallelogramTest(panel, renderer); });
		//		 break;
		//case 3:	 lineDrawerPage((panel, renderer)->{ new RandomLineTest(panel, renderer); });
		//		 break;
		//case 4:  polygonDrawerPage(panels);
		//		 break;
		//case 0:	 polygonDrawerPage(ghostPanels);		// will be fifth page.  5 == 0 (mod 5)
		//		 break;
		//default: defaultPage();
		//		 break;
		//}
	}

	@FunctionalInterface
	private interface TestPerformer {
		public void perform(Drawable drawable, LineRenderer renderer);
	}
	private void lineDrawerPage(TestPerformer test) {
		image.clear();

		for(int panelNumber = 0; panelNumber < panels.length; panelNumber++) {
			panels[panelNumber].clear();
			test.perform(panels[panelNumber], lineRenderers[panelNumber]);
		}
	}
	public void polygonDrawerPage(Drawable[] panelArray) {
		image.clear();
		for(Drawable panel: panels) {		// 'panels' necessary here.  Not panelArray, because clear() uses setPixel.
			panel.clear();
		}
		//new StarburstPolygonTest(panelArray[0], polygonRenderer);
		//new MeshPolygonTest(panelArray[1], polygonRenderer, MeshPolygonTest.NO_PERTURBATION);
		//new MeshPolygonTest(panelArray[2], polygonRenderer, MeshPolygonTest.USE_PERTURBATION);
		//new RandomPolygonTest(panelArray[3], polygonRenderer);
	}

	private void defaultPage() {
		image.clear();
		largePanel.fill(ARGB_GREEN, Double.MAX_VALUE);
	}

}
