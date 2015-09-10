package hu.gdf.terepimeres.activity.project;

import hu.gdf.terepimeres.R;
import hu.gdf.terepimeres.entity.Gcp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TwoLineAdapter extends BaseAdapter {
  
  private static List<ProjectItem> searchArray;
  private Context                  context;
  
  private LayoutInflater           mInflater;
  
  private final Drawable           no_img;
  private final Drawable           tracklog_img;
  
  public TwoLineAdapter(Context context, List<ProjectItem> results) {
    searchArray = new ArrayList<ProjectItem>(results);
    mInflater = LayoutInflater.from(context);
    no_img = context.getResources().getDrawable(R.drawable.no_img);
    tracklog_img = context.getResources().getDrawable(R.drawable.ic_tracklog);
    this.context = context;
  }
  
  @Override
  public int getCount() {
    return searchArray.size();
  }
  
  @Override
  public Object getItem(int position) {
    return searchArray.get(position);
  }
  
  @Override
  public long getItemId(int position) {
    return position;
  }
  
  @Override
  public View getView(int position,
                      View convertView,
                      ViewGroup parent) {
    final ViewHolder holder;
    if ( convertView == null ) {
      /*
       * mInflater.inflate(resource, root); ezt ne használd!!!
       */
      // ezt pedig nem szabad true-val hívni (API hiba)
      convertView = mInflater.inflate(R.layout.project_item, parent, false);
      
      holder = new ViewHolder();
      holder.name = (TextView) convertView.findViewById(R.id.proj_list_adap_gcp_name);
      holder.decrLeft = (TextView) convertView.findViewById(R.id.proj_list_adap_descr_left);
      holder.decrRight = (TextView) convertView.findViewById(R.id.proj_list_adap_descr_right);
      holder.img = (ImageView) convertView.findViewById(R.id.proj_list_adap_img);
      
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }
    
    holder.position = position;
    
    ProjectItem projectElement = searchArray.get(position);
    holder.projectItem = projectElement;
    holder.decrLeft.setText(projectElement.getDescrLeft(context));
    holder.decrRight.setText(projectElement.getDescrRight(context));
    String name = projectElement.getName();
    if ( projectElement instanceof Gcp ) {
      holder.name.setText(name);
      holder.img.setImageDrawable(no_img);
      final Gcp gcp = (Gcp) projectElement;
      final File img;
      if ( gcp.getImgs().size() > 0 && (img = gcp.getImgs().iterator().next()) != null
          && img.exists() ) {
        
        new AsyncTask<Integer, Void, Bitmap>() {
          
          private Integer pos;
          
          @Override
          protected Bitmap doInBackground(Integer... param) {
            pos = param[0];
            return hu.gdf.terepimeres.activity.Utils.getBitmap(img.getAbsolutePath(), 128);
          }
          
          @Override
          protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if ( pos.intValue() == holder.position ) {
              holder.img.setImageBitmap(result);
            }
          }
        }.execute(position);
      }
    } else {
      if ( "".equals(name) ) {
        holder.name.setText("Recording...");
      } else {
        holder.name.setText(name);
      }
      holder.img.setImageDrawable(tracklog_img);
    }
    
    return convertView;
  }
  
  void addProjectItem(ProjectItem item) {
    searchArray.add(item);
  }
  
  static class ViewHolder {
    
    int         position;
    ProjectItem projectItem;
    TextView    name;
    TextView    decrLeft;
    TextView    decrRight;
    ImageView   img;
  }
  
}
