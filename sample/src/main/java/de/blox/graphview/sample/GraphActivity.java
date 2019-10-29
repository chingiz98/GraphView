package de.blox.graphview.sample;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import de.blox.graphview.BaseGraphAdapter;
import de.blox.graphview.Edge;
import de.blox.graphview.Graph;
import de.blox.graphview.GraphAdapter;
import de.blox.graphview.GraphView;
import de.blox.graphview.Node;
import de.blox.graphview.Size;
import de.blox.graphview.Vector;
import de.blox.graphview.ViewHolder;
import de.blox.graphview.tree.BuchheimWalkerAlgorithm;
import de.blox.graphview.tree.BuchheimWalkerConfiguration;

public class GraphActivity extends AppCompatActivity {
    private int nodeCount = 1;
    private int finalsCount = 1;
    private Node currentNode;
    protected BaseGraphAdapter<ViewHolder> adapter;

    private RadioGroup rg;
    private RadioButton andRadio;
    private RadioButton orRadio;
    private RadioButton termRadio;
    private EditText captionText;
    private EditText probText;
    private EditText dmgText;

    private Button addBtn;
    private Button removeBtn;
    private Button calculateBtn;
    private Button saveBtn;
    private Button loadBtn;

    private Node root = null;

    private String formula;


    int checkedRadio = 1;
    Graph graph;

    Graph loadedGraph = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_buchheim_walker_orientations, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final BuchheimWalkerConfiguration.Builder builder = new BuchheimWalkerConfiguration.Builder()
                .setSiblingSeparation(100)
                .setLevelSeparation(300)
                .setSubtreeSeparation(300);

        switch (item.getItemId()) {
            case R.id.topToBottom:
                builder.setOrientation(BuchheimWalkerConfiguration.ORIENTATION_TOP_BOTTOM);
                break;
            case R.id.bottomToTop:
                builder.setOrientation(BuchheimWalkerConfiguration.ORIENTATION_BOTTOM_TOP);
                break;
            case R.id.leftToRight:
                builder.setOrientation(BuchheimWalkerConfiguration.ORIENTATION_LEFT_RIGHT);
                break;
            case R.id.rightToLeft:
                builder.setOrientation(BuchheimWalkerConfiguration.ORIENTATION_RIGHT_LEFT);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        adapter.setAlgorithm(new BuchheimWalkerAlgorithm(builder.build()));
        adapter.notifyInvalidated();
        return true;
    }

    View.OnClickListener addClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (currentNode != null && (currentNode.getData()).getType() == NodeData.TYPE_TERM) {
                Toast.makeText(GraphActivity.this, "Узел является инициатором.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (captionText.getText().toString().equals("")) {
                Toast.makeText(GraphActivity.this, "Введите текст", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentNode != null) {
                Node newNode;
                if (checkedRadio == NodeData.TYPE_TERM) {
                    double p = Double.parseDouble(probText.getText().toString());



                    if (probText.getText().toString().equals("")) {
                        Toast.makeText(GraphActivity.this, "Введите вероятность", Toast.LENGTH_SHORT).show();
                        return;
                    }



                    newNode = new Node(new de.blox.graphview.NodeData("[x" + (finalsCount) + "] "
                            + captionText.getText().toString() + "[P = " + p + "]", p, checkedRadio, "x" + finalsCount));
                    finalsCount++;
                } else {
                    newNode = new Node(new de.blox.graphview.NodeData(captionText.getText().toString(), checkedRadio));
                }

                graph.addEdge(currentNode, newNode);
                captionText.setText("");
            }

            if (graph.getNodeCount() == 0) {
                final Node newNode = new Node(new de.blox.graphview.NodeData(captionText.getText().toString(), checkedRadio));
                root = newNode;
                //graph.addEdge(currentNode, newNode);
                graph.addNode(newNode);
                adapter.notifyNodeAdded(newNode);
                captionText.setText("");

            }
        }
    };

    View.OnClickListener removeClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (currentNode != null) {
                graph.removeNode(currentNode);
                currentNode = null;
            }
        }
    };

    View.OnClickListener calcClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            List<Node> nodeList = graph.getNodes();
            for (Node n : nodeList) {
                if (n.getData().getType() != NodeData.TYPE_TERM && graph.successorsOf(n).size() < 2) {
                    Toast.makeText(GraphActivity.this, "Неккоректное дерево", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            String rez = DFS(root);

            //1-(1-(1-(1-x1)*(1-x2))) * (1-x3*x4)
            boolean flag = false;
            while (!flag) {
                if (rez.contains("!!"))
                    rez = rez.replace("!!", "");
                else
                    flag = true;

            }
            Log.d("a", rez);
            flag = false;
            while (!flag) {
                for (int i = 0; i < graph.getNodes().size(); i++) {
                    if (rez.contains("!x" + i))
                        rez = rez.replace("!x" + i, "(1-x" + i + ")");
                    else
                        flag = true;
                }
            }
            String bracket = Pattern.quote(")");
            String anotherBracket = Pattern.quote("!(");
            Log.d("a", rez);


            String sas = rez;//"!((!x1*!x2)*!(x3*x4))";
            String res = "";
            int counter = 0;
            int toInsert = 0;
            for(int i = 0; i < sas.length(); i++){
                if(sas.charAt(i) == '!' && sas.charAt(i + 1) == '('){
                    //i++;
                    res += "(1-";
                    toInsert++;
                    counter = 0;
                    continue;
                } else if (sas.charAt(i) == '('){
                    counter++;
                } else if (sas.charAt(i) == ')'){
                    counter--;
                    if(counter == 0 && toInsert > 0){
                        //i++;
                        String temp = "";
                        for(int j = 0; j < toInsert; j++){
                            temp += ')';
                        }
                        res += temp;

                        toInsert = 0;
                    }
                }
                res += sas.charAt(i);
            }

            for(int i = 0; i < toInsert; i++){
                res += ')';
            }


            Log.d("a", res);

            /*
            Expression calc = new ExpressionBuilder(res)
                    .variable("x1")

                    .variable("x2")
                    .variable("x3")
                    .variable("x4")

                    .build();
                    *
             */

            ExpressionBuilder eb = new ExpressionBuilder(res);

            for(Node n : graph.getNodes()){
                if((n.getData()).getType() == NodeData.TYPE_TERM){
                    eb.variable(( n.getData()).getVar());
                }
            }
            Expression calc = eb.build();


            for(Node n : graph.getNodes()){
                if(( n.getData()).getType() == NodeData.TYPE_TERM){
                    eb.variable(( n.getData()).getVar());
                    calc.setVariable(( n.getData()).getVar(), ( n.getData()).getVal());
                }
            }

            /*
            calc.setVariable("x1", 0.5)
                    .setVariable("x2", 0.5)
                    .setVariable("x3", 0.5)
                    .setVariable("x4", 0.5);

             */




            double result1 = calc.evaluate();
            Log.d("a", String.valueOf(result1));

            double dmg = 0.0;
            if(dmgText.getText().toString().equals("")){
                Toast.makeText(GraphActivity.this, "Введите ущерб", Toast.LENGTH_SHORT).show();
            } else {
                dmg = Double.parseDouble(dmgText.getText().toString());
            }




            AlertDialog.Builder builder = new AlertDialog.Builder(GraphActivity.this);
            builder.setTitle("Внимание")
                    .setMessage("ФАЛ: " + rez + "\nВыражение: " + res + "\nP= " + result1 + "\nРезультат: "
                            + dmg * result1)
                    .setCancelable(false)
                    .setNegativeButton("ОК",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();

        }
    };


    private void saveGraphData() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        /*
        ArrayList<Node> list = new ArrayList<Node>();
        for(Node n : graph.getNodes()){
            list.add(n);
        }
        */

        String json = gson.toJson(graph);
        editor.putString("task list1", json);
        editor.apply();
    }

    private void loadGraphData() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("task list1", null);
        Log.d("a", json);
        Type type = new TypeToken<Graph>() {}.getType();
        Graph list = gson.fromJson(json, type);

        /*
        if (mExampleList == null) {
            mExampleList = new ArrayList<>();
        }
        */
        loadedGraph = list;


        setupAdapter(loadedGraph);

    }



    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        ArrayList<Edge> list = new ArrayList<Edge>();
        for(Edge n : graph.getEdges()){
            list.add(n);
        }
        String json = gson.toJson(list);
        editor.putString("task list", json);
        editor.apply();

    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("task list", null);
        Log.d("a", json);
        Type type = new TypeToken<ArrayList<Edge>>() {}.getType();
        ArrayList<Edge> list = gson.fromJson(json, type);

        /*
        if (mExampleList == null) {
            mExampleList = new ArrayList<>();
        }
        */

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        andRadio = findViewById(R.id.and_rb);
        orRadio = findViewById(R.id.or_rb);
        termRadio = findViewById(R.id.term_rb);
        captionText = findViewById(R.id.caption_et);
        probText = findViewById(R.id.prob_et);
        dmgText = findViewById(R.id.damage_et);
        rg = findViewById(R.id.radio_group);

        addBtn = findViewById(R.id.add_btn);
        removeBtn = findViewById(R.id.remove_btn);
        calculateBtn = findViewById(R.id.calc_btn);
        saveBtn = findViewById(R.id.save_btn);
        loadBtn = findViewById(R.id.load_btn);

        addBtn.setOnClickListener(addClick);
        removeBtn.setOnClickListener(removeClick);
        calculateBtn.setOnClickListener(calcClick);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                saveData();
                saveGraphData();
            }
        });

        loadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData();
                loadGraphData();
            }
        });



        graph = createGraph();
        setupToolbar();
        setupAdapter(graph);

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton checkedRadioButton = (RadioButton) rg.findViewById(i);
                // This puts the value (true/false) into the variable
                boolean isChecked = checkedRadioButton.isChecked();
                if (isChecked) {
                    if (i == R.id.and_rb) {
                        checkedRadio = NodeData.TYPE_AND;
                        probText.setInputType(InputType.TYPE_NULL);
                    }
                    if (i == R.id.or_rb) {
                        checkedRadio = NodeData.TYPE_OR;
                        probText.setInputType(InputType.TYPE_NULL);
                    }
                    if (i == R.id.term_rb) {
                        checkedRadio = NodeData.TYPE_TERM;
                        probText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    }
                }
            }
        });
    }

    private void setupAdapter(Graph graph) {
        final GraphView graphView = findViewById(R.id.graph);

        adapter = new BaseGraphAdapter<ViewHolder>(graph) {

            @Override
            public int getItemViewType(int position) {
                int type = graph.getNode(position).getData().getType();
                return type;
            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                final View view;
                switch (viewType) {
                    case NodeData.TYPE_AND:
                        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.and_node, parent, false);
                        return new AndViewHolder(view);
                    case NodeData.TYPE_OR:
                        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.or_node, parent, false);
                        return new OrViewHolder(view);
                    case NodeData.TYPE_TERM:
                        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.terminal_node, parent, false);
                        return new TermViewHolder(view);
                    default:
                        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.or_node, parent, false);
                        return new AndViewHolder(view);
                }
            }

            @Override
            public void onBindViewHolder(ViewHolder viewHolder, Object data, int position) {

                int type = (graph.getNode(position).getData()).getType();

                switch (type) {
                    case NodeData.TYPE_AND:
                        ((AndViewHolder) viewHolder).textView.setText(((de.blox.graphview.NodeData) data).getText());
                        break;
                    case NodeData.TYPE_OR:
                        ((OrViewHolder) viewHolder).textView.setText(((de.blox.graphview.NodeData) data).getText());
                        break;
                    case NodeData.TYPE_TERM:
                        ((TermViewHolder) viewHolder).textView.setText(((de.blox.graphview.NodeData) data).getText());
                        break;
                    default:
                        ((AndViewHolder) viewHolder).textView.setText(((de.blox.graphview.NodeData) data).getText());
                        break;
                }

            }

            class SimpleViewHolder extends ViewHolder {
                TextView textView;

                SimpleViewHolder(View itemView) {
                    super(itemView);
                    textView = itemView.findViewById(R.id.textView);
                }
            }

            class AndViewHolder extends ViewHolder {
                TextView textView;

                AndViewHolder(View itemView) {
                    super(itemView);
                    textView = itemView.findViewById(R.id.and_text);
                }
            }

            class OrViewHolder extends ViewHolder {
                TextView textView;

                OrViewHolder(View itemView) {
                    super(itemView);
                    textView = itemView.findViewById(R.id.or_text);
                }
            }

            class TermViewHolder extends ViewHolder {
                TextView textView;

                TermViewHolder(View itemView) {
                    super(itemView);
                    textView = itemView.findViewById(R.id.term_text);
                }
            }


        };

        setAlgorithm(adapter);

        graphView.setAdapter(adapter);
        graphView.setOnItemClickListener((parent, view, position, id) -> {
            currentNode = adapter.getNode(position);
        });
    }


    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public Graph createGraph() {
        final Graph graph = new Graph();


        final Node node1 = new Node(new de.blox.graphview.NodeData(getNodeText(), NodeData.TYPE_AND));
        final Node node2 = new Node(new de.blox.graphview.NodeData(getNodeText(), NodeData.TYPE_OR));
        final Node node3 = new Node(new de.blox.graphview.NodeData(getNodeText(), NodeData.TYPE_OR));
        final Node node4 = new Node(new de.blox.graphview.NodeData(getNodeText(), 0.5, NodeData.TYPE_TERM, "x1"));
        final Node node5 = new Node(new de.blox.graphview.NodeData(getNodeText(), 0.5, NodeData.TYPE_TERM, "x2"));
        final Node node6 = new Node(new de.blox.graphview.NodeData(getNodeText(), 0.5, NodeData.TYPE_TERM, "x3"));
        final Node node7 = new Node(new de.blox.graphview.NodeData(getNodeText(), 0.5, NodeData.TYPE_TERM, "x4"));

        graph.addEdge(node1, node2);
        graph.addEdge(node1, node3);
        graph.addEdge(node2, node4);
        graph.addEdge(node2, node5);
        graph.addEdge(node3, node6);
        graph.addEdge(node3, node7);

        root = node1;



        return graph;
    }


    public String DFS(Node currentNode) {

        List<Node> successors;
        if (graph.hasSuccessor(currentNode)) {
            successors = graph.successorsOf(currentNode);
            String a = "";
            for (int i = 0; i < successors.size(); i++) {


                if (currentNode.getData().getType() == NodeData.TYPE_AND) {
                    if (i == graph.successorsOf(currentNode).size() - 1)
                        a += DFS(successors.get(i)) + ")";
                    else if (i == 0) {
                        a += "(" + DFS(successors.get(i)) + "*";
                    } else {
                        a += DFS(successors.get(i)) + "*";
                    }


                } else {
                    {
                        if (a.equals("")) {
                            a += "!(!" + DFS(successors.get(i)) + "*";
                        } else if (i == graph.successorsOf(currentNode).size() - 1) {
                            a += "!" + DFS(successors.get(i)) + ")";
                        } else {
                            a += "!" + DFS(successors.get(i));
                        }


                    }


                }

            }

            return a;

        } else {
            return (currentNode.getData().getVar());
        }

    }


    public void setAlgorithm(GraphAdapter adapter) {
        final BuchheimWalkerConfiguration configuration = new BuchheimWalkerConfiguration.Builder()
                .setSiblingSeparation(100)
                .setLevelSeparation(300)
                .setSubtreeSeparation(300)
                .setOrientation(BuchheimWalkerConfiguration.ORIENTATION_TOP_BOTTOM)
                .build();
        adapter.setAlgorithm(new BuchheimWalkerAlgorithm(configuration));
    }

    protected String getNodeText() {
        return "Node " + nodeCount++;
    }
}
