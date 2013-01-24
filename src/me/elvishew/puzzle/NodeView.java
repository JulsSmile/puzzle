package me.elvishew.puzzle;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

public class NodeView extends View {

    private static final String TAG = NodeView.class.getSimpleName();

    private Node mNode;

    public NodeView(Context context) {
        super(context);
    }

    public NodeView(Context context, Node node) {
        this(context);
        mNode = node;
    }

    public void bindNode(Node node) {
        mNode = node;
    }

    public Node getNode() {
        return mNode;
    }

    public static void exchangeContent(NodeView nv1, NodeView nv2) {
        Node.exchangeContent(nv1.mNode, nv2.mNode);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mNode == null) {
            return;
        }

        setMeasuredDimension(
                resolveSize(mNode.getNodeSize(), widthMeasureSpec),
                resolveSize(mNode.getNodeSize(), heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mNode == null) {
            return;
        }
        mNode.draw(canvas);

    }
}