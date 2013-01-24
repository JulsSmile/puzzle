package me.elvishew.puzzle;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class Node {
    private int mSlotPosition = 0;
    private int mCurrentContent = 0;
    
    private Bitmap mBitmap = null;

    private Rect mSrcRect;
    private Rect mDstRect;

    private int mSize;
    private int mNodeSize;

    private Paint mExitPaint = new Paint();

    public Node(int size, int nodesize, Bitmap bitmap, int exitColor){
        mSize = size;
        mNodeSize = nodesize;
        mBitmap = bitmap;
        
        mSrcRect = new Rect();
        mDstRect = new Rect(0, 0, nodesize - 1, nodesize - 1);

        mExitPaint.setColor(exitColor);
    }

    public void set(int size, int nodesize, Bitmap bitmap, int exitColor) {
        mSize = size;
        mNodeSize = nodesize;
        mBitmap = bitmap;
        mExitPaint.setColor(exitColor);
        mDstRect.set(0, 0, nodesize - 1, nodesize - 1);
    }

    public int getNodeSize() {
        return mNodeSize;
    }

    public Node setBitmap(Bitmap bitmap){
        mBitmap = bitmap;
        return this;
    }

    public Node setSlotPosition(int position){
        mSlotPosition = position;
        return this;
    }
    
    public int getSlotPosition(){
        return mSlotPosition;
    }
    
    public Node setCurrentContent(int content){
        mCurrentContent = content;
        return this;
    }
    
    public int getCurrentContent(){
        return mCurrentContent;
    }
    
    public static void exchangeContent(Node node1, Node node2){
        
        /* Exchange content */
        int tmp = node1.getCurrentContent();
        node1.setCurrentContent(node2.getCurrentContent());
        node2.setCurrentContent(tmp);
    }

    public void draw(Canvas canvas) {
        /* Draw exit image if this node is the last */
        if (mCurrentContent == mSize * mSize - 1){
            mSrcRect.set(0, 0, mNodeSize - 1, mNodeSize - 1);
// FIXME            canvas.drawRect(mDstRect, mExitPaint);
        } else if (mBitmap != null){
            /*should be modified*/
            int srcX = (mCurrentContent % mSize) * mNodeSize;
            int srcY = (mCurrentContent / mSize) * mNodeSize;
            
            mSrcRect.set(srcX, srcY, srcX + mNodeSize - 1, srcY + mNodeSize - 1);
            
            canvas.drawBitmap(mBitmap, mSrcRect, mDstRect, null);
        }
    }

    class Position {
        private int x;
        private int y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void set(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Position)) {
                return false;
            }
            Position other = (Position) o;
            return ((other != null) && (x == other.x) && (y == other.y));
        }
    }
}