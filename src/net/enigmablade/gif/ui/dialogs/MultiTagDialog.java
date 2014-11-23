package net.enigmablade.gif.ui.dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.stream.*;
import com.alee.extended.panel.*;
import com.alee.laf.rootpane.*;
import com.alee.laf.panel.*;
import com.alee.laf.button.*;
import com.alee.laf.scroll.*;
import com.alee.laf.list.*;
import com.alee.laf.list.editor.*;
import com.alee.laf.label.*;
import net.enigmablade.gif.img.*;
import net.enigmablade.gif.search.*;
import net.enigmablade.gif.ui.components.item.*;
import net.enigmablade.gif.ui.renderers.*;

@SuppressWarnings("unchecked")
public class MultiTagDialog extends WebDialog
{
	//Components
	private WebPanel kagamineLen;
	private ItemImage imagePanel;
	private WebList mikuList, lukaList;
	private WebButton vFlower, kokone;
	
	//Data
	
	private DefaultListModel<String> tagsListModel, recentTagsListModel;
	
	//Initialization
	
	public MultiTagDialog()
	{
		initFrame();
		initComponents();
		pack();
		initModels();
		initListeners();
	}
	
	private void initFrame()
	{
		setTitle("Select tags");
		setModal(true);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setPreferredSize(new Dimension(450, 450));
	}
	
	private void initComponents()
	{
		WebPanel vocaloid = new WebPanel();
		vocaloid.setMargin(2);
		setContentPane(vocaloid);
		
		WebPanel v2 = new WebPanel();
		vocaloid.add(v2, BorderLayout.CENTER);
		GridBagLayout gbl_v2 = new GridBagLayout();
		gbl_v2.columnWidths = new int[]{200, 200, 0};
		gbl_v2.rowHeights = new int[]{200, 0, 0, 0};
		gbl_v2.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_v2.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		v2.setLayout(gbl_v2);
		
		WebScrollPane hatsuneMiku = new WebScrollPane(null);
		GridBagConstraints gbc_hatsuneMiku = new GridBagConstraints();
		gbc_hatsuneMiku.gridheight = 3;
		gbc_hatsuneMiku.fill = GridBagConstraints.BOTH;
		gbc_hatsuneMiku.gridx = 0;
		gbc_hatsuneMiku.gridy = 0;
		v2.add(hatsuneMiku, gbc_hatsuneMiku);
		
		mikuList = new WebList();
		mikuList.setEditable(true);
		mikuList.setCellRenderer(new CustomWebListCellRenderer());
		hatsuneMiku.setViewportView(mikuList);
		
		imagePanel = new ItemImage(null);
		
		kagamineLen = new CenterPanel(imagePanel);
		kagamineLen.setUndecorated(false);
		GridBagConstraints gbc_webPanel = new GridBagConstraints();
		gbc_webPanel.fill = GridBagConstraints.BOTH;
		gbc_webPanel.gridx = 1;
		gbc_webPanel.gridy = 0;
		v2.add(kagamineLen, gbc_webPanel);
		
		
		WebLabel kagamineRin = new WebLabel();
		kagamineRin.setFontSize(14);
		kagamineRin.setText("Recent tags");
		GridBagConstraints gbc_kagamineRin = new GridBagConstraints();
		gbc_kagamineRin.gridx = 1;
		gbc_kagamineRin.gridy = 1;
		v2.add(kagamineRin, gbc_kagamineRin);
		
		WebScrollPane megurineLuka = new WebScrollPane(null);
		GridBagConstraints gbc_megurineLuka = new GridBagConstraints();
		gbc_megurineLuka.fill = GridBagConstraints.BOTH;
		gbc_megurineLuka.gridx = 1;
		gbc_megurineLuka.gridy = 2;
		v2.add(megurineLuka, gbc_megurineLuka);
		
		lukaList = new WebList();
		megurineLuka.setViewportView(lukaList);
		
		WebPanel v3 = new WebPanel();
		vocaloid.add(v3, BorderLayout.SOUTH);
		v3.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		
		vFlower = new WebButton();
		vFlower.setLeftRightSpacing(12);
		vFlower.setText("OK");
		v3.add(vFlower);
		
		kokone = new WebButton();
		kokone.setLeftRightSpacing(12);
		kokone.setText("Cancel");
		v3.add(kokone);
	}
	
	private void initListeners()
	{
		//Tag list
		mikuList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt)
			{
				if(SwingUtilities.isLeftMouseButton(evt) && evt.getClickCount() == 2)
				{
					addNewTag();
				}
				else if(SwingUtilities.isRightMouseButton(evt))
				{
					int i = mikuList.locationToIndex(evt.getPoint());
					if(i >= 0)
					{
						tagsListModel.remove(i);
					}
				}
			}
		});
		
		mikuList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent evt)
			{
				if(evt.getKeyCode() == KeyEvent.VK_DELETE)
				{
					int[] selected = mikuList.getSelectedIndices();
					for(int n = 0; n < selected.length; n++)
						tagsListModel.remove(selected[n] - n);
				}
				else if(evt.getKeyCode() == KeyEvent.VK_SPACE)
				{
					if(!mikuList.isEditing())
						addNewTag();
				}
			}
		});
		
		mikuList.addListEditListener(new ListEditListener() {
			@Override
			public void editStarted(int index) {}
			
			@Override
			public void editFinished(int index, Object oldValue, Object newValue)
			{
				vFlower.setEnabled(true);
			}
			
			@Override
			public void editCancelled(int index)
			{
				if("".equals(tagsListModel.get(index)))
					tagsListModel.remove(index);
			}
		});
		
		//Recent tags list
		
		lukaList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt)
			{
				if((SwingUtilities.isLeftMouseButton(evt) && evt.getClickCount() == 2) || SwingUtilities.isRightMouseButton(evt))
				{
					int i = lukaList.locationToIndex(evt.getPoint());
					if(i >= 0)
					{
						String value = recentTagsListModel.get(i);
						tagsListModel.addElement(value);
						recentTagsListModel.remove(i);
					}
				}
			}
		});
		
		//Window buttons
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evt)
			{
				closeDialog();
			}
		});
		
		//OK
		vFlower.addActionListener((evt) -> {
			//TODO: Add tags to returnTags
			closeDialog();
		});
		
		//Cancel
		kokone.addActionListener((evt) -> {
			closeDialog();
		});
	}
	
	private void initModels()
	{
		mikuList.setModel(tagsListModel = new DefaultListModel<>());
		lukaList.setModel(recentTagsListModel = new DefaultListModel<>());
	}
	
	//External functionality
	
	private static MultiTagDialog instance;
	
	public static Set<String> showDialog(ImageFrame[] image, TagCache recentTags)
	{
		if(instance == null)
			instance = new MultiTagDialog();
		return instance.openDialog(image, recentTags);
	}
	
	public Set<String> openDialog(ImageFrame[] image, TagCache recentTags)
	{
		reset();
		//TODO: reverse tag add order
		recentTags.forEach((tag) -> recentTagsListModel.addElement(tag));
		
		if(image != null && image.length > 0)
		{
			int maxWidth = kagamineLen.getWidth()-8;
			int maxHeight = kagamineLen.getHeight()-8;
			if(image[0].getWidth() > image[0].getHeight())
				maxHeight = (int)(maxWidth*1.0*image[0].getHeight()/image[0].getWidth());
			else if(image[0].getWidth() < image[0].getHeight())
				maxWidth = (int)(maxHeight*1.0*image[0].getWidth()/image[0].getHeight());
			imagePanel.setPreferredSize(maxWidth, maxHeight);
			imagePanel.setSize(maxWidth, maxHeight);
			kagamineLen.doLayout();
			kagamineLen.revalidate();
		}
		imagePanel.startAnimation(image);
		
		addNewTag();
		setVisible(true);
		
		imagePanel.stopAnimation();
		return Collections.list(tagsListModel.elements()).stream().collect(Collectors.toSet());
	}
	
	public void closeDialog()
	{
		dispose();
	}
	
	//Internal functionality
	
	private void addNewTag()
	{
		if(!mikuList.isEditing())
		{
			tagsListModel.addElement("");
			mikuList.editCell(tagsListModel.size()-1);
		}
	}
	
	private void reset()
	{
		tagsListModel.clear();
		recentTagsListModel.clear();
		
		vFlower.setEnabled(false);
	}
}
