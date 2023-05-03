package edu.cuhk.cuhk_all_in_one.adapters;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.amrdeveloper.treeview.TreeNode;
import com.amrdeveloper.treeview.TreeViewHolder;

import edu.cuhk.cuhk_all_in_one.R;
import edu.cuhk.cuhk_all_in_one.activities.NotesActivity;
import edu.cuhk.cuhk_all_in_one.datatypes.NoteEntry;
import edu.cuhk.cuhk_all_in_one.datatypes.NotePin;
import edu.cuhk.cuhk_all_in_one.datatypes.NoteTag;

public class NotesTreeViewHolder extends TreeViewHolder {

    private TextView fileName;
    private ImageView fileStateIcon;
    private ImageView fileTypeIcon;

    public NotesTreeViewHolder(@NonNull View itemView) {
        super(itemView);
        initViews();
    }

    private void initViews() {
        fileName = itemView.findViewById(R.id.file_name);
        fileStateIcon = itemView.findViewById(R.id.file_state_icon);
        fileTypeIcon = itemView.findViewById(R.id.file_type_icon);
    }

    @Override
    public void bindTreeNode(TreeNode node) {
        super.bindTreeNode(node);
        // Here you can bind your node and check if it selected or not

        // A node can be a pin, a tag or a note entry, check and render list accordingly
        if (node.getValue() instanceof NotePin) {
            fileName.setText(((NotePin) node.getValue()).pinName);
            fileTypeIcon.setImageResource(R.drawable.ic_baseline_location_on_24);
        } else if (node.getValue() instanceof NoteTag) {
            fileName.setText(((NoteTag) node.getValue()).tagName);
            fileTypeIcon.setImageResource(R.drawable.ic_baseline_local_offer_24);
        } else if (node.getValue() instanceof NoteEntry) {
            fileName.setText(((NoteEntry) node.getValue()).noteTitle);
            fileTypeIcon.setImageResource(R.drawable.ic_baseline_insert_drive_file_24);
        }

        // Toggle expand icon
        if (node.getChildren().isEmpty()) {
            fileStateIcon.setVisibility(View.INVISIBLE);
        } else {
            fileStateIcon.setVisibility(View.VISIBLE);
            int stateIcon = node.isExpanded() ? R.drawable.ic_baseline_expand_less_24 : R.drawable.ic_baseline_expand_more_24;
            fileStateIcon.setImageResource(stateIcon);
        }

        // Go to note activity if a note is clicked
        if (node.getValue() instanceof NoteEntry && node.isSelected()) {
            Intent noteIntent = new Intent(itemView.getContext(), NotesActivity.class);
            noteIntent.putExtra("noteUid", ((NoteEntry) node.getValue()).uid);
            noteIntent.putExtra("fromTreeActivity", true);
            itemView.getContext().startActivity(noteIntent);
        }
    }
}
