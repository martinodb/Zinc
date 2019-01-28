/*
 * JungGraphPanel.java
 *
 * Created on Nov 28, 2009, 7:52:32 PM
 */

package edu.buffalo.cse.sneps3.gui;

import edu.buffalo.cse.sneps3.gui.business.Caseframe;
import edu.buffalo.cse.sneps3.gui.business.IView;
import edu.buffalo.cse.sneps3.gui.business.Slot;
import edu.buffalo.cse.sneps3.gui.business.SemanticType;
import edu.buffalo.cse.sneps3.gui.business.Term;
import edu.buffalo.cse.sneps3.gui.graph.ArrowFillTransformer;
import edu.buffalo.cse.sneps3.gui.graph.ArrowShapeTransformer;
import edu.buffalo.cse.sneps3.gui.graph.ChannelEdge;
import edu.buffalo.cse.sneps3.gui.graph.IEdge;
import edu.buffalo.cse.sneps3.gui.graph.ITermNode;
import edu.buffalo.cse.sneps3.gui.graph.RestrictionEdge;
import edu.buffalo.cse.sneps3.gui.graph.SnepsGraph;
import edu.buffalo.cse.sneps3.gui.graph.SnepsModalGraphMouse;
import edu.buffalo.cse.sneps3.gui.graph.TermNode;
import edu.buffalo.cse.sneps3.gui.graph.Edge;
import edu.buffalo.cse.sneps3.gui.graph.CollapsedEdge;
import edu.buffalo.cse.sneps3.gui.graph.DependencyEdge;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.event.GraphEvent.Vertex;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.EdgeShape.Line;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.control.ModalLensGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.EdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import edu.uci.ics.jung.visualization.transform.HyperbolicTransformer;
import edu.uci.ics.jung.visualization.transform.LayoutLensSupport;
import edu.uci.ics.jung.visualization.transform.LensSupport;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import edu.uci.ics.jung.graph.util.DefaultParallelEdgeIndexFunction;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.decorators.EdgeShape.QuadCurve;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.media.j3d.*;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.vecmath.Point3d;
import org.apache.commons.collections15.Transformer;

/**
 * @author Daniel R. Schlegel
 */
public class JungGraphPanel extends javax.swing.JPanel implements IView {

	private static final long serialVersionUID = 6788099320573599178L;

	enum GraphType {
		FR3d, FR2d
	};

    boolean showNewTerms = true;
    boolean showOntologyTerms = false;
	
	boolean straightEdges = true;
	boolean findv2 = true;

	boolean showing_all = true;

	GraphType type = GraphType.FR2d;

	QuadCurve<ITermNode<IEdge>, IEdge> quadcurve = new EdgeShape.QuadCurve<ITermNode<IEdge>, IEdge>();
	Line<ITermNode<IEdge>, IEdge> line = new EdgeShape.Line<ITermNode<IEdge>, IEdge>();

	SnepsGraph<ITermNode<IEdge>, IEdge> dsg;
	VisualizationViewer<ITermNode<IEdge>, IEdge> vv;
	VisualizationModel<ITermNode<IEdge>, IEdge> vm;

	// No idea what i need of these yet.
	LensSupport hyperbolicViewSupport;
	LensSupport hyperbolicLayoutSupport;

	SnepsModalGraphMouse<ITermNode<IEdge>, IEdge> graphMouse = new SnepsModalGraphMouse<ITermNode<IEdge>, IEdge>();

	public HashSet<ITermNode<IEdge>> highlightedNodes = new HashSet<ITermNode<IEdge>>();
	Color highlightedColor = new Color(135, 206, 250);

	edu.uci.ics.jung.algorithms.layout.AbstractLayout<ITermNode<IEdge>, IEdge> layout;

	final ScalingControl scaler = new CrossoverScalingControl();
	int lastSliderVal = 0;

	int displayAreaWidth = 700;
	int displayAreaHeight = 350;

	final float dash[] = { 10.0f };
	final Stroke dashStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
	
	final float dot[] = { 2.0f, 2.0f };
	final Stroke dotStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER, 10.0f, dot, 0.0f);
	
	final float dashdot[] = { 10.0f, 5.0f, 2.0f, 5.0f };
	final Stroke dashdotStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER, 10.0f, dashdot, 0.0f);
	
	final Stroke solidStroke = new BasicStroke(1.0f);

	public int scale_btn_click = 0;
	float scaleAmt = 1.2f;

	public static JungGraphPanel instance;

	GraphZoomScrollPane panel;

	/** Creates new form JungGraphPanel */
	public JungGraphPanel() {
		instance = this;
		initComponents();

		dsg = new SnepsGraph<ITermNode<IEdge>, IEdge>();
		configureGraphViewer();
		displayGraph(dsg);
		
		GUI2.getModel().registerView(this);
	}

	public VisualizationViewer<ITermNode<IEdge>, IEdge> getVV() {
		return vv;
	}

	public void setFontSize(final int size) {
		Transformer<ITermNode<IEdge>, Font> vertexFont = new Transformer<ITermNode<IEdge>, Font>() {

			public Font transform(ITermNode<IEdge> i) {
				return new Font(vv.getFont().getName(),
						vv.getFont().getStyle(), size);
			}
		};

		vv.setFont(new Font(vv.getFont().getName(), vv.getFont().getStyle(),
				size));

		vv.getRenderContext().setVertexFontTransformer(vertexFont);
	}

	public SnepsGraph<ITermNode<IEdge>, IEdge> getGraph() {
		return dsg;
	}

	public void setShowNewTerms(boolean b){
		showNewTerms = b;
	}
	
	public boolean getShowNewTerms(){
		return showNewTerms;
	}
	
	public void setShowOntologyTerms(boolean b){
		showOntologyTerms = b;
	}
	
	public boolean getShowOntologyTerms(){
		return showOntologyTerms;
	}
	
	public void configureGraphViewer(){
		vm = new DefaultVisualizationModel<ITermNode<IEdge>, IEdge>(new edu.uci.ics.jung.algorithms.layout.FRLayout2<ITermNode<IEdge>, IEdge>(dsg));
		vv = new VisualizationViewer<ITermNode<IEdge>, IEdge>(vm);
		
		vv.setBackground(Color.white);

		vv.getRenderContext().setVertexLabelTransformer(
				new ToStringLabeller<ITermNode<IEdge>>());
		vv.getRenderer().getVertexLabelRenderer()
				.setPosition(Position.CNTR);

		quadcurve.setEdgeIndexFunction(DefaultParallelEdgeIndexFunction.getInstance());
		
		if (straightEdges) {
			vv.getRenderContext()
					.setEdgeShapeTransformer(
							new Transformer<Context<Graph<ITermNode<IEdge>, IEdge>, IEdge>, Shape>() {
								public Shape transform(
										Context<Graph<ITermNode<IEdge>, IEdge>, IEdge> i) {
									if (i.element instanceof RestrictionEdge || i.element instanceof DependencyEdge) {
										return quadcurve.transform(i);
									}

									// Check for parallel edges:
									ITermNode<IEdge> n = i.element.getFrom();
									for (Object j : dsg.getOutEdges(n)) {
										IEdge e = (IEdge) j;
										if (e.getTo() == i.element.getTo()
												&& i.element != e) {
											return quadcurve.transform(i);
										}
									}
									return line.transform(i);
								}
							});

			vv.getRenderer().setEdgeLabelRenderer(
					new CustomEdgeLabelRenderer<ITermNode<IEdge>, IEdge>());
		}

		vv.getRenderContext().setEdgeStrokeTransformer(
				new Transformer<IEdge, Stroke>() {

					public Stroke transform(IEdge i) {
						if (i instanceof RestrictionEdge || i instanceof DependencyEdge) {
							return dashStroke;
						}
						else if (i instanceof ChannelEdge) {
							if (((ChannelEdge) i).getType() == ChannelEdge.ChannelType.ICHANNEL)
								return dotStroke;
							return dashdotStroke;
						}
						return solidStroke;
					}

				});

		// Makes the labels more closely really centered on the line.
		vv.getRenderContext()
				.setEdgeLabelClosenessTransformer(
						new Transformer<edu.uci.ics.jung.graph.util.Context<Graph<ITermNode<IEdge>, IEdge>, IEdge>, Number>() {

							public Number transform(
									Context<Graph<ITermNode<IEdge>, IEdge>, IEdge> i) {
								return new Double(.5);
							}

						});

		vv.getRenderContext().setEdgeLabelTransformer(
				new ToStringLabeller<IEdge>());

		vv.getRenderContext().setEdgeArrowTransformer(
				new ArrowShapeTransformer<Object, Object>());

		vv.getRenderContext().setVertexShapeTransformer(
				new NodeShapeTransformer<Object, Object>());

		Transformer<ITermNode<IEdge>, Paint> vertexPaint = new Transformer<ITermNode<IEdge>, Paint>() {
			public Paint transform(ITermNode<IEdge> i) {

				PickedState<ITermNode<IEdge>> ps = vv.getPickedVertexState();

				if (ps.isPicked(i))
					return Color.green;
				else if (highlightedNodes.contains(i))
					return highlightedColor;
				else if (i.getTerm().getActivation() == 1.0)
					return Color.red;
				else if (i.getTerm().getActivation() > 0.40)
					return Color.orange;
				else if (i.getTerm().getActivation() > 0)
					return Color.yellow;
				else
					return Color.pink;
			}
		};

		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);

		vv.getRenderContext().setArrowFillPaintTransformer(
				new ArrowFillTransformer<IEdge, Paint>());

		vv.setGraphMouse(graphMouse);
		graphMouse.setMode(Mode.PICKING);

		hyperbolicLayoutSupport = new LayoutLensSupport<ITermNode<IEdge>, IEdge>(vv,
				new HyperbolicTransformer(vv, vv.getRenderContext()
						.getMultiLayerTransformer()
						.getTransformer(Layer.LAYOUT)),
				new ModalLensGraphMouse());

		graphMouse.addItemListener(hyperbolicLayoutSupport.getGraphMouse()
				.getModeListener());
		
	}
	
	public void displayGraph(SnepsGraph<ITermNode<IEdge>, IEdge> g) {
		if (jToggleButton_collapse.isSelected()) {
			jToggleButton_collapse.setSelected(false);
			g.expandGraph();
			//semanticExpandAll();
		}

		dsg = g;
		// The Layout<V, E> is parameterized by the vertex and edge types
		// We should probably use the FRLayout(g) which is used by the current
		// show().
		// *****2d version******//
		if (type == GraphType.FR2d) {
			layout = new edu.uci.ics.jung.algorithms.layout.FRLayout2<ITermNode<IEdge>, IEdge>(g);
			// layout.setSize(new Dimension(this.getWidth(), this.getHeight()));
			int layoutwidth = (this.getWidth() < 100 ? 700 : this.getWidth()
					- ((Integer) UIManager.get("ScrollBar.width")).intValue()
					- 15);
			int layoutheight = (this.getHeight() < 100 ? 350 : this.getHeight()
					- ((Integer) UIManager.get("ScrollBar.width")).intValue()
					- 40);

			layout.setSize(new Dimension(layoutwidth, layoutheight));
			
			DefaultVisualizationModel<ITermNode<IEdge>, IEdge> vm = new DefaultVisualizationModel<ITermNode<IEdge>, IEdge>(layout);
			
			vv.setModel(vm);

			panel = new GraphZoomScrollPane(vv);
			if (((BorderLayout) this.getLayout())
					.getLayoutComponent(BorderLayout.CENTER) != null)
				this.remove(((BorderLayout) this.getLayout())
						.getLayoutComponent(BorderLayout.CENTER));
			add(panel, BorderLayout.CENTER);
		}
		// *****3d version*****//
		if (type == GraphType.FR3d) {
			edu.uci.ics.jung.algorithms.layout3d.Layout<ITermNode<IEdge>, IEdge> layout3d = new edu.uci.ics.jung.algorithms.layout3d.FRLayout<ITermNode<IEdge>, IEdge>(
					g);
			layout3d.setSize(new BoundingSphere(new Point3d(150, 150, 150), 50)); // sets
																					// the
																					// initial
																					// size
																					// of
																					// the
																					// space

			edu.uci.ics.jung.visualization3d.VisualizationViewer<ITermNode<IEdge>, IEdge> vv3d = new edu.uci.ics.jung.visualization3d.VisualizationViewer<ITermNode<IEdge>, IEdge>();
			vv3d.setGraphLayout(layout3d);
			vv3d.setPreferredSize(new Dimension(350, 350)); // Sets the viewing
															// area size
			add(vv3d, BorderLayout.CENTER);
		}

		// on_graph_expanded_nodenames = new ArrayList<String>();
		/*on_graph_expanded_nodenames.clear();
		for (ITermNode<IEdge> o : dsg.getVertices()) {
			on_graph_expanded_nodenames.add(o.getTerm().getName());
		}*/

	}

	public void addVertex(ITermNode<IEdge> n) {
		dsg.addVertex(n);
		n.show();
		//on_graph_expanded_nodenames.add(n.getTerm().getName());
	}

	public boolean addEdge(Edge e) {
		if (e instanceof RestrictionEdge)
			return dsg.addRestrictionEdge((RestrictionEdge)e, new Pair<ITermNode<IEdge>>(e.getFrom(), e.getTo()));
		
		if (e instanceof DependencyEdge)
			return dsg.addDependencyEdge((DependencyEdge)e, new Pair<ITermNode<IEdge>>(e.getFrom(), e.getTo()));
		
		return dsg.addEdge(e, e.getFrom(), e.getTo(), EdgeType.DIRECTED);
	}

	public void reinitialize() {
		jToggleButton_collapse.setSelected(false);
		hideAll();
		showing_all = true;
	}

	public void hideAll() {
		dsg.hideAll();
		vv.repaint();
		showing_all = false;
	}

	public void showAll() {
		dsg.showAll();
		vv.repaint();
		showing_all = true;
	}

	public boolean isEmpty() {
		return (dsg.getVertexCount() == 0);
	}

	public boolean isShowingAll() {
		return showing_all;
	}
	
	public void displayOnlyTermSet(Set<Term> terms){
		hideAll();
		displayTermSet(terms);
	}
	
	public void displayTermSet(Set<Term> terms){
		highlightedNodes.clear();
		for(Term t : terms){
			dsg.showVertex(dsg.getVertex(t.getName()));
			highlightedNodes.add(dsg.getVertex(t.getName()));
			for(ITermNode<IEdge> n : dsg.getSuccessors(dsg.getVertex(t.getName())))
				highlightedNodes.add(n);
		}
		vv.repaint();
	}

	public void hideNode(ITermNode<IEdge> n ){
		dsg.hideVertex(n);
		vv.repaint();
	}
	
	public void showNode(ITermNode<IEdge> n){
		dsg.showVertex(n);
		vv.repaint();
	}

	public void setStatusbarText(String t) {
		jLabel_status.setText(t);
	}

	/*
	 * public Vector<IEdge> edgesOf(TermNode n){ Vector<IEdge> v = new
	 * Vector<IEdge>(); while(dsg.getEdges().iterator().hasNext()){ Edge e =
	 * (Edge)dsg.getEdges().iterator().next(); dsg.get }
	 * 
	 * return v; }
	 */

	private void initComponents() {

		jToolBar1 = new javax.swing.JToolBar();
		jButton_showInGraph = new javax.swing.JButton();
		jButton_hideAll = new javax.swing.JButton();
		jButton_showAll = new javax.swing.JButton();
		jLabel2 = new javax.swing.JLabel();
		jComboBox_graphMode = new javax.swing.JComboBox<String>();
		//jComboBox_layout = new javax.swing.JComboBox();
		jSeparator2 = new javax.swing.JToolBar.Separator();
		jSeparator3 = new javax.swing.JToolBar.Separator();
		//jSeparator4 = new javax.swing.JToolBar.Separator();
		jLabel1 = new javax.swing.JLabel();
		jToggleButton_lens = new javax.swing.JToggleButton();
		jToggleButton_collapse = new javax.swing.JToggleButton();
		jPanel1 = new javax.swing.JPanel();
		jToolBar2 = new javax.swing.JToolBar();
		jLabel3 = new javax.swing.JLabel();
		//jLabel_layout = new javax.swing.JLabel();
		jButton_scalePlus = new javax.swing.JButton();
		jButton_scaleMinus = new javax.swing.JButton();
		jButton_scaleReset = new javax.swing.JButton();
		jLabel_status = new javax.swing.JLabel();

		jToolBar1.setRollover(true);

		jButton_showInGraph.setText("Show In Graph");
		jButton_showInGraph.setFocusable(false);
		jButton_showInGraph
				.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		jButton_showInGraph
				.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
		jButton_showInGraph
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jButton_showInGraphActionPerformed(evt);
					}
				});
		jToolBar1.add(jButton_showInGraph);

		jButton_hideAll.setText("Hide All");
		jButton_hideAll.setFocusable(false);
		jButton_hideAll
				.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		jButton_hideAll
				.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
		jButton_hideAll.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton_hideAllActionPerformed(evt);
			}
		});
		jToolBar1.add(jButton_hideAll);

		jButton_showAll.setText("Show All");
		jButton_showAll.setFocusable(false);
		jButton_showAll
				.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		jButton_showAll
				.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
		jButton_showAll.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton_showAllActionPerformed(evt);
			}
		});
		jToolBar1.add(jButton_showAll);

		jToolBar1.add(jSeparator3);
		
		jLabel2.setText("Mouse:");
		jToolBar1.add(jLabel2);

		jComboBox_graphMode.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] {
				"Picking", "Transforming" }));
		jComboBox_graphMode.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jComboBox1ActionPerformed(evt);
			}
		});
		jToolBar1.add(jComboBox_graphMode);
		
//		jToolBar1.add(jSeparator4);
//		
//		jLabel_layout.setText("Layout:");
//		jToolBar1.add(jLabel_layout);
//		
//		jComboBox_layout.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
//				"Force Based", "Balloon", "Radial" }));
//		jComboBox_layout.addActionListener(new java.awt.event.ActionListener() {
//			public void actionPerformed(java.awt.event.ActionEvent evt) {
//				jComboBox_layoutActionPerformed(evt);
//			}
//		});
//		jToolBar1.add(jComboBox_layout);
		
		
		
		jToolBar1.add(jSeparator2);

		jLabel1.setText("View:");
		jToolBar1.add(jLabel1);

		jToggleButton_lens.setText("Lens");
		jToggleButton_lens
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jToggleButton_lensActionPerformed(evt);
					}
				});
		jToolBar1.add(jToggleButton_lens);

		jToggleButton_collapse.setText("Collapsed");
		jToggleButton_collapse
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jToggleButton_collapseActionPerformed(evt);
					}
				});
		jToolBar1.add(jToggleButton_collapse);

		jToolBar2.setFloatable(false);
		jToolBar2.setRollover(true);

		jLabel3.setFont(new java.awt.Font("Ubuntu", 0, 12));
		jLabel3.setText("Zoom:");
		jToolBar2.add(jLabel3);

		jButton_scalePlus.setFont(new java.awt.Font("Ubuntu", 0, 12));
		jButton_scalePlus.setText(" + ");
		jButton_scalePlus.setFocusable(false);
		jButton_scalePlus
				.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		jButton_scalePlus
				.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
		jButton_scalePlus
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jButton_scalePlusActionPerformed(evt);
					}
				});
		jToolBar2.add(jButton_scalePlus);

		jButton_scaleMinus.setFont(new java.awt.Font("Ubuntu", 0, 12));
		jButton_scaleMinus.setText(" - ");
		jButton_scaleMinus.setFocusable(false);
		jButton_scaleMinus
				.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		jButton_scaleMinus
				.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
		jButton_scaleMinus
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jButton_scaleMinusActionPerformed(evt);
					}
				});
		jToolBar2.add(jButton_scaleMinus);

		jButton_scaleReset.setFont(new java.awt.Font("Ubuntu", 0, 12));
		jButton_scaleReset.setText(" Reset ");
		jButton_scaleReset.setFocusable(false);
		jButton_scaleReset
				.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		jButton_scaleReset
				.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
		jButton_scaleReset
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						jButton_scaleResetActionPerformed(evt);
					}
				});
		jToolBar2.add(jButton_scaleReset);

		jLabel_status.setText(" ");

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(
				jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout
				.setHorizontalGroup(jPanel1Layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								jPanel1Layout
										.createSequentialGroup()
										.addComponent(jLabel_status)
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED,
												652, Short.MAX_VALUE)
										.addComponent(
												jToolBar2,
												javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE)));
		jPanel1Layout.setVerticalGroup(jPanel1Layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(jLabel_status)
				.addComponent(jToolBar2,
						javax.swing.GroupLayout.PREFERRED_SIZE, 18,
						Short.MAX_VALUE));

		setLayout(new BorderLayout());
		add(jToolBar1, BorderLayout.NORTH);
		add(jPanel1, BorderLayout.SOUTH);

		Border emptyBorder = BorderFactory.createEmptyBorder();
		jButton_scalePlus.setBorder(emptyBorder);
		jButton_scaleMinus.setBorder(emptyBorder);
		jButton_scaleReset.setBorder(emptyBorder);
	}

	private void jToggleButton_collapseActionPerformed(
			java.awt.event.ActionEvent evt) {
		if (jToggleButton_collapse.isSelected()) {
			dsg.collapseGraph();
		} else {
			dsg.expandGraph();
		}
		vv.repaint();
	}

	private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {
		if (jComboBox_graphMode.getSelectedItem().equals("Picking"))
			graphMouse.setMode(Mode.PICKING);
		else
			graphMouse.setMode(Mode.TRANSFORMING);
	}
	
//	private void jComboBox_layoutActionPerformed(java.awt.event.ActionEvent evt) {
//		//vm.setGraphLayout(new edu.uci.ics.jung.algorithms.layout.DAGLayout<ITermNode<IEdge>, IEdge>(dsg));
//		
//		//new edu.uci.ics.jung.algorithms.layout.FRLayout<ITermNode<IEdge>, IEdge>(dsg))
//	}

	private void jToggleButton_lensActionPerformed(
			java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jToggleButton_lensActionPerformed
		if (jToggleButton_lens.isSelected())
			hyperbolicLayoutSupport.activate();
		else
			hyperbolicLayoutSupport.deactivate();

		if (jComboBox_graphMode.getSelectedItem().equals("Picking"))
			graphMouse.setMode(Mode.PICKING);
		else
			graphMouse.setMode(Mode.TRANSFORMING);
	}// GEN-LAST:event_jToggleButton_lensActionPerformed

	private void jButton_scalePlusActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton_scalePlusActionPerformed
		scaler.scale(vv, scaleAmt, vv.getCenter());
		this.scale_btn_click++;
	}// GEN-LAST:event_jButton_scalePlusActionPerformed

	private void jButton_scaleMinusActionPerformed(
			java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton_scaleMinusActionPerformed
		scaler.scale(vv, 1 / scaleAmt, vv.getCenter());
		this.scale_btn_click--;
	}// GEN-LAST:event_jButton_scaleMinusActionPerformed

	private void jButton_scaleResetActionPerformed(
			java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton_scaleResetActionPerformed
		vv.getRenderContext().getMultiLayerTransformer()
				.getTransformer(Layer.LAYOUT).setToIdentity();
		vv.getRenderContext().getMultiLayerTransformer()
				.getTransformer(Layer.VIEW).setToIdentity();
		this.scale_btn_click = 0;
	}// GEN-LAST:event_jButton_scaleResetActionPerformed

	private void jButton_hideAllActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton_hideAllActionPerformed
		hideAll();
	}// GEN-LAST:event_jButton_hideAllActionPerformed

	private void jButton_showAllActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton_showAllActionPerformed
		showAll();
	}// GEN-LAST:event_jButton_showAllActionPerformed

	private void jButton_showInGraphActionPerformed(
			java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton_showInGraphActionPerformed
		FindQuery3 f = new FindQuery3();
		f.setVisible(true);
	}// GEN-LAST:event_jButton_showInGraphActionPerformed

	private javax.swing.JButton jButton_hideAll;
	private javax.swing.JButton jButton_scaleMinus;
	private javax.swing.JButton jButton_scalePlus;
	private javax.swing.JButton jButton_scaleReset;
	private javax.swing.JButton jButton_showAll;
	private javax.swing.JButton jButton_showInGraph;
	private javax.swing.JComboBox<String> jComboBox_graphMode;
	//private javax.swing.JComboBox jComboBox_layout;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel_status;
	private javax.swing.JPanel jPanel1;
	//private javax.swing.JLabel jLabel_layout;
	private javax.swing.JToolBar.Separator jSeparator2;
	private javax.swing.JToolBar.Separator jSeparator3;
	//private javax.swing.JToolBar.Separator jSeparator4;
	private javax.swing.JToggleButton jToggleButton_collapse;
	private javax.swing.JToggleButton jToggleButton_lens;
	private javax.swing.JToolBar jToolBar1;
	private javax.swing.JToolBar jToolBar2;

	class NodeShapeTransformer<St, Ss> implements Transformer<ITermNode<IEdge>, Shape> {

		// private DirectedSparseMultigraph<?, ?> d;

		// public NodeShapeTransformer(DirectedSparseMultigraph dsg){
		// d = dsg;
		// }

		public Shape transform(ITermNode<IEdge> arg0) {
			// int width = 13*arg0.toString().length();

			int width = vv.getFontMetrics(vv.getFont()).stringWidth(
					arg0.toString()) + 10;

			return new RoundRectangle2D.Float((width / 2) * -1, -7, width, 15,
					5, 5);

			// return new java.awt.Rectangle((width/2)*-1, -7, width, 15);
		}

	}

	class CustomEdgeLabelRenderer<V, E> implements Renderer.EdgeLabel<V, E> {

		public Component prepareRenderer(RenderContext<V, E> rc,
				EdgeLabelRenderer graphLabelRenderer, Object value,
				boolean isSelected, E edge) {
			return rc.getEdgeLabelRenderer().<E> getEdgeLabelRendererComponent(
					rc.getScreenDevice(), value,
					rc.getEdgeFontTransformer().transform(edge), isSelected,
					edge);
		}

		public void labelEdge(RenderContext<V, E> rc,
				edu.uci.ics.jung.algorithms.layout.Layout<V, E> layout, E e,
				String label) {
			if (label == null || label.length() == 0)
				return;
			
			if(e instanceof CollapsedEdge){
				label = label + (((CollapsedEdge)e).getReplacedTerm().getTerm().isAsserted() ? "!" : "");
			}

			Graph<V, E> graph = layout.getGraph();
			// don't draw edge if either incident vertex is not drawn
			Pair<V> endpoints = graph.getEndpoints(e);
			V v1 = endpoints.getFirst();
			V v2 = endpoints.getSecond();
			if (!rc.getEdgeIncludePredicate().evaluate(
					Context.<Graph<V, E>, E> getInstance(graph, e)))
				return;

			if (!rc.getVertexIncludePredicate().evaluate(
					Context.<Graph<V, E>, V> getInstance(graph, v1))
					|| !rc.getVertexIncludePredicate().evaluate(
							Context.<Graph<V, E>, V> getInstance(graph, v2)))
				return;

			Point2D p1 = layout.transform(v1);
			Point2D p2 = layout.transform(v2);
			p1 = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p1);
			p2 = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p2);
			float x1 = (float) p1.getX();
			float y1 = (float) p1.getY();
			float x2 = (float) p2.getX();
			float y2 = (float) p2.getY();

			GraphicsDecorator g = rc.getGraphicsContext();
			float distX = x2 - x1;
			float distY = y2 - y1;
			double totalLength = Math.sqrt(distX * distX + distY * distY);

			double closeness = rc.getEdgeLabelClosenessTransformer()
					.transform(Context.<Graph<V, E>, E> getInstance(graph, e))
					.doubleValue();

			int posX = (int) (x1 + (closeness) * distX);
			int posY = (int) (y1 + (closeness) * distY);

			int xDisplacement = (int) (rc.getLabelOffset() * (distY / totalLength));
			int yDisplacement = (int) (rc.getLabelOffset() * (-distX / totalLength));

			Component component = prepareRenderer(rc,
					rc.getEdgeLabelRenderer(), label, rc.getPickedEdgeState()
							.isPicked(e), e);

			Dimension d = component.getPreferredSize();

			Shape edgeShape = rc.getEdgeShapeTransformer().transform(
					Context.<Graph<V, E>, E> getInstance(graph, e));

			double parallelOffset = 1;

			// System.err.println(e.toString() + ": " +
			// rc.getParallelEdgeIndexFunction().getIndex(graph, e));

			if (rc.getParallelEdgeIndexFunction().getIndex(graph, e) > 0) {
				parallelOffset += rc.getParallelEdgeIndexFunction().getIndex(
						graph, e);

				parallelOffset *= d.height;
				if (edgeShape instanceof Ellipse2D) {
					parallelOffset += edgeShape.getBounds().getHeight();
					parallelOffset = -parallelOffset;
				}
			} else
				parallelOffset = 20;

			AffineTransform old = g.getTransform();
			AffineTransform xform = new AffineTransform(old);
			xform.translate(posX + xDisplacement, posY + yDisplacement);
			double dx = x2 - x1;
			double dy = y2 - y1;
			if (rc.getEdgeLabelRenderer().isRotateEdgeLabels()) {
				double theta = Math.atan2(dy, dx);
				if (dx < 0) {
					theta += Math.PI;
				}
				xform.rotate(theta);
			}
			if (dx < 0) {
				parallelOffset = -parallelOffset;
			}

			xform.translate(-d.width / 2, -(d.height / 2 - parallelOffset));
			g.setTransform(xform);
			g.draw(component, rc.getRendererPane(), 0, 0, d.width, d.height,
					true);

			g.setTransform(old);
		}

	}

	public void ctUpdate(ArrayList<edu.buffalo.cse.sneps3.gui.business.Context> c, Boolean clear) {
		vv.repaint();
	}

	public void ctCurrent(edu.buffalo.cse.sneps3.gui.business.Context c) {
	}

	public void stUpdate(Collection<SemanticType> v, Boolean clear) {
	}

	public void cfUpdate(Collection<Caseframe> cf, boolean clear) {
	}

	public void slotUpdate(Collection<Slot> slot, Boolean clear) {
	}
	
	public boolean shouldShowTerm(ITermNode<IEdge> tn) {
		if (showNewTerms && !tn.isVisible()) {
			//System.out.println("Ont term? " + tn.getTerm() + " " + tn.getTerm().isOntologyTerm());

			if (showOntologyTerms) return true;
			else if (!tn.getTerm().isOntologyTerm()) return true;
		}
		return false;
	}

	
    // Warning: Do not refer to up-cablesetw here on atomic nodes unless you already have
    // the molecular node! It may still be being constructed!
	public void termUpdate(Collection<Term> term, Boolean clear) {
		if (clear) {
			dsg.removeAll();
			vv.repaint();
			return;
		}

		// Put all the term nodes on the graph.
		for (Term t : term) {
			// Try to find the term in the graph.
			ITermNode<IEdge> tn = dsg.getVertex(t.getName());
			if (tn != null) {
				// Show the node only if in a state requiring it.
				if (shouldShowTerm(tn))
					showNode(tn);
				return; // Is this needed? It's old code, but it seems like a bug, not sure. [DRS
						// 1/3/18]
			} else {
				// Term not already in the graph.
				// Always add to the graph, defaults to hidden.
				tn = new TermNode<IEdge>(t);
				if (GUI2.DEBUG)
					System.err.println("Adding node: " + tn);
				dsg.addVertex(tn);
				// Show only if needed.
				if (shouldShowTerm(tn))
					showNode(tn);
			}
		}

		for (Term t : term) {
			ITermNode<IEdge> tn = dsg.getVertex(t.getName());

			// Deal with edges within molecular terms.
			if (t.isMolecular()) {
				HashMap<Slot, Set<Term>> dcs = t.getDownCableset();
				for (Entry<Slot, Set<Term>> entry : dcs.entrySet()) {
					for (Term endterm : entry.getValue()) {
						ITermNode<IEdge> targetnode = dsg.getVertex(endterm.getName());

						// It's possible that one of the arguments of a new molecular node is hidden.
						// Make sure to make it visible.
						if (tn.isVisible() && !targetnode.isVisible())
							showNode(targetnode);

						Edge edge = new Edge(entry.getKey(), tn, targetnode);
						if (GUI2.DEBUG)
							System.out.println(
									"Adding edge from: " + tn.getTerm().getName() + " to: " + endterm.getName());
						if (!addEdge(edge))
							System.err.println("Error adding edge from: " + tn + " to: " + endterm);
					}
				}
			}

			// Deal with "metaedges" within variable terms (restriction + depends edges).
			else if (t.isVariable()) {
				Set<Term> rs = t.getRestrictionset();
				for (Term r : rs) {
					RestrictionEdge edge = new RestrictionEdge(tn, dsg.getVertex(r.getName()));
					if (!addEdge(edge))
						System.err.println("Error adding edge from: " + tn + " to: " + r.getName());
				}

				Set<Term> deps = t.getDependencies();
				for (Term d : deps) {
					DependencyEdge edge = new DependencyEdge(tn, dsg.getVertex(d.getName()));
					if (!addEdge(edge))
						System.err.println("Error adding edge from: " + tn + " to: " + d.getName());
				}
			}
		}

		vv.repaint();

	}

}
