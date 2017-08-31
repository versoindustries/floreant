package com.floreantpos.demo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

import com.floreantpos.Messages;
import com.floreantpos.POSConstants;
import com.floreantpos.actions.LogoutAction;
import com.floreantpos.config.TerminalConfig;
import com.floreantpos.main.Application;
import com.floreantpos.model.KitchenTicket;
import com.floreantpos.model.OrderType;
import com.floreantpos.model.dao.KitchenTicketDAO;
import com.floreantpos.swing.PaginatedListModel;
import com.floreantpos.swing.PosUIManager;
import com.floreantpos.ui.dialog.NumberSelectionDialog2;
import com.floreantpos.ui.dialog.POSMessageDialog;
import com.floreantpos.ui.views.order.RootView;

public class KitchenTicketListPanel extends JPanel implements ComponentListener {
	private final static int HORIZONTAL_GAP = 5;
	private final static int VERTICAL_GAP = 5;

	protected PaginatedListModel dataModel = new PaginatedListModel();
	protected Dimension buttonSize = PosUIManager.getSize(350, 240);
	protected JPanel selectionButtonsPanel;

	protected TitledBorder border;
	protected JPanel actionButtonPanel = new JPanel(new MigLayout("fillx,hidemode 3, ins 0 5 0 5", "[grow][]", "[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	private StyledButton btnLogout;
	private StyledButton btnFilter;
	private StyledButton btnBack;

	protected StyledButton btnNext;
	protected StyledButton btnPrev;

	private List<String> kdsPrinters;
	private int horizontalPanelCount = 4;
	private List<OrderType> orderTypes;

	private List<String> selectedPrinters;
	private List<OrderType> selectedOrderTypes;

	public KitchenTicketListPanel() {
		this.selectionButtonsPanel = new JPanel(new MigLayout("fillx")) { //$NON-NLS-1$
			@Override
			public void remove(Component comp) {
				updateKDSView();
			}
		};
		setLayout(new BorderLayout(HORIZONTAL_GAP, VERTICAL_GAP));
		border = new TitledBorder(""); //$NON-NLS-1$
		border.setTitleJustification(TitledBorder.CENTER);
		setBorder(new CompoundBorder(border, new EmptyBorder(2, 2, 2, 2)));
		selectionButtonsPanel.setBackground(Color.black);
		add(selectionButtonsPanel);

		btnBack = new StyledButton(Messages.getString("KitchenDisplayView.1")); //$NON-NLS-1$
		btnBack.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				RootView.getInstance().showDefaultView();
			}
		});

		btnFilter = new StyledButton(Messages.getString("KitchenDisplayView.2")); //$NON-NLS-1$
		btnFilter.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				KitchenFilterDialog dialog = new KitchenFilterDialog();
				dialog.setSelectedPrinters(selectedPrinters);
				dialog.setSelectedOrderTypes(selectedOrderTypes);
				dialog.open();

				if (dialog.isCanceled())
					return;

				getDataModel().setCurrentRowIndex(0);
				selectedPrinters = dialog.getSelectedPrinters();
				selectedOrderTypes = dialog.getSelectedOrderTypes();

				updateKDSView();
			}
		});

		JPanel optionPanel = new JPanel(new MigLayout("ins 0,hidemode 3", "", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		optionPanel.setOpaque(false);
		Dimension size = PosUIManager.getSize(90, 40);

		optionPanel.setBackground(Color.white);

		StyledButton btnOption = new StyledButton(Messages.getString("KitchenDisplayView.8")); //$NON-NLS-1$
		btnOption.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int value = NumberSelectionDialog2.takeIntInput(Messages.getString("KitchenDisplayView.9")); //$NON-NLS-1$
				if (value == -1)
					return;
				TerminalConfig.setKDSTicketsPerPage(value);
				updateKDSView();
			}
		});

		btnLogout = new StyledButton("Logout"); //$NON-NLS-1$
		btnLogout.setAction(new LogoutAction(true, false));

		optionPanel.add(btnLogout, "w " + size.width + "!, h " + size.height + "!"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		optionPanel.add(btnBack, "w " + size.width + "!, h " + size.height + "!"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		optionPanel.add(btnFilter, "w " + size.width + "!,h " + size.height + "!"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		optionPanel.add(btnOption, "w " + size.width + "!, h " + size.height + "!"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		actionButtonPanel.add(optionPanel);

		btnPrev = new StyledButton(POSConstants.CAPITAL_PREV + "<<");
		actionButtonPanel.add(btnPrev, "split 2,right,w " + size.width + "!, h " + size.height + "!"); //$NON-NLS-1$

		btnNext = new StyledButton(POSConstants.CAPITAL_NEXT + ">>");
		actionButtonPanel.add(btnNext, "right, w " + size.width + "!, h " + size.height + "!"); //$NON-NLS-1$

		add(actionButtonPanel, BorderLayout.SOUTH);
		actionButtonPanel.setOpaque(false);

		ScrollAction action = new ScrollAction();
		btnPrev.addActionListener(action);
		btnNext.addActionListener(action);

		addComponentListener(this);
		btnNext.setBackground(Color.white);
		btnPrev.setBackground(Color.white);
		btnNext.setForeground(Color.black);
		btnPrev.setForeground(Color.black);
		btnNext.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		btnPrev.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		btnNext.setEnabled(false);
		btnPrev.setEnabled(false);
	}

	public void checkNewKitchenTicket(KitchenTicket ticket) {
	}

	public void updateKDSView() {
		reset();
		try {
			dataModel.setPageSize(TerminalConfig.getKDSTicketsPerPage());
			dataModel.setNumRows(KitchenTicketDAO.getInstance().getRowCount(selectedPrinters, orderTypes));
			KitchenTicketDAO.getInstance().loadKitchenTickets(selectedPrinters, orderTypes, dataModel);
			setDataModel(dataModel);
		} catch (Exception e) {
			POSMessageDialog.showError(Application.getPosWindow(), e.getMessage(), e);
		}
	}

	public void setTitle(String title) {
		border.setTitle(title);
	}

	public void setDataModel(PaginatedListModel items) {
		this.dataModel = items;
		renderItems();
	}

	protected void updateButton() {
		boolean hasNext = dataModel.hasNext();
		boolean hasPrevious = dataModel.hasPrevious();
		btnNext.setVisible(hasNext || hasPrevious);
		btnPrev.setVisible(hasNext || hasPrevious);
		btnPrev.setEnabled(hasPrevious);
		btnNext.setEnabled(hasNext);
	}

	public PaginatedListModel getDataModel() {
		return dataModel;
	}

	public Dimension getButtonSize() {
		return buttonSize;
	}

	public void setButtonSize(Dimension buttonSize) {
		this.buttonSize = buttonSize;
	}

	public void reset() {
		Component[] components = selectionButtonsPanel.getComponents();
		for (Component component : components) {
			if (component instanceof KitchenTicketView) {
				KitchenTicketView kitchenTicketView = (KitchenTicketView) component;
				kitchenTicketView.stopTimer();
			}
		}
		selectionButtonsPanel.removeAll();
		selectionButtonsPanel.repaint();
		btnNext.setEnabled(false);
		btnPrev.setEnabled(false);
	}

	protected int getCount(int containerSize, int itemSize) {
		int panelCount = containerSize / itemSize;
		return panelCount;
	}

	protected int countPanels() {
		Dimension size = Application.getInstance().getRootView().getSize();
		horizontalPanelCount = getCount(size.width, 330);
		int verticalPanelCount = getCount(size.height, 280);
		int totalItem = horizontalPanelCount * verticalPanelCount;
		return totalItem;
	}

	protected void renderItems() {
		try {
			reset();

			if (this.dataModel == null || dataModel.getSize() == 0) {
				btnNext.setEnabled(dataModel.hasNext());
				btnPrev.setEnabled(dataModel.hasPrevious());
				return;
			}
			String parentConstraints = "filly"; //$NON-NLS-1$
			if (horizontalPanelCount >= 3 && dataModel.getSize() >= 3) {
				parentConstraints = "fill"; //$NON-NLS-1$
			}
			selectionButtonsPanel.setLayout(new MigLayout(parentConstraints + ", wrap " + horizontalPanelCount, "sg, fill", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			for (int i = 0; i < dataModel.getSize(); i++) {
				Object item = dataModel.getElementAt(i);
				JPanel itemPanel = createKitchenTicket(item, i);
				if (itemPanel == null) {
					continue;
				}
				String contraints = "growy"; //$NON-NLS-1$
				if (horizontalPanelCount >= 3 && dataModel.getSize() >= 3) {
					contraints = "grow"; //$NON-NLS-1$
				}
				selectionButtonsPanel.add(itemPanel, contraints);
			}
			btnNext.setEnabled(dataModel.hasNext());
			btnPrev.setEnabled(dataModel.hasPrevious());
			revalidate();
			repaint();
		} catch (Exception e) {
			POSMessageDialog.showError(this, e.getMessage(), e);
		}
	}

	private class ScrollAction implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			try {
				Object source = e.getSource();
				if (source == btnPrev) {
					scrollUp();
				}
				else if (source == btnNext) {
					scrollDown();
				}
			} catch (Exception e2) {
				POSMessageDialog.showError(Application.getPosWindow(), e2.getMessage(), e2);
			}
		}

	}

	public JPanel getButtonsPanel() {
		return selectionButtonsPanel;
	}

	public AbstractButton getFirstItemButton() {
		int componentCount = selectionButtonsPanel.getComponentCount();
		if (componentCount == 0) {
			return null;
		}
		return (AbstractButton) selectionButtonsPanel.getComponent(0);
	}

	protected int getButtonCount(int containerSize, int itemSize) {
		int buttonCount = containerSize / (itemSize + 10);
		return buttonCount;
	}

	protected void scrollDown() {
		dataModel.setCurrentRowIndex(dataModel.getNextRowIndex());
		updateKDSView();
	}

	protected void scrollUp() {
		dataModel.setCurrentRowIndex(dataModel.getPreviousRowIndex());
		updateKDSView();
	}

	protected JPanel createKitchenTicket(Object item, int index) {
		KitchenTicketView kitchenTicketView = new KitchenTicketView((KitchenTicket) item);
		kitchenTicketView.putClientProperty("key", (index + 1)); //$NON-NLS-1$
		return kitchenTicketView;
	}

	@Override
	public void componentResized(ComponentEvent e) {
		if (!KitchenDisplayView.getInstance().isVisible())
			return;
		countPanels();
		updateKDSView();
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	public void setBackButtonVisible(boolean b) {
		btnBack.setVisible(b);
	}

}
