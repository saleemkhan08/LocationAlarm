package co.thnki.locationalarm.ads;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public abstract class RecyclerViewAdapterBase<Model, RowLayout extends View>
        extends RecyclerView.Adapter<ViewWrapper<RowLayout>>
{
    @Override
    public abstract int getItemCount();

    @Override
    public final ViewWrapper<RowLayout> onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return new ViewWrapper<>(onCreateItemView(parent, viewType));
    }

    protected abstract RowLayout onCreateItemView(ViewGroup parent, int viewType);

    // additional methods to manipulate the items

    public abstract Model getItem(int position);
}
