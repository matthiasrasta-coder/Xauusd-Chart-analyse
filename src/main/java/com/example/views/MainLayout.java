package com.example.views;

import com.example.data.CandleService;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;

/** Rahmen um alle Seiten: Titelleiste mit Navigation. */
public class MainLayout extends AppLayout {

    public MainLayout(CandleService candleService) {
        Span title = new Span(candleService.getSymbol() + " · MetaTrader 5");
        title.addClassName("app-title");

        VerticalLayout nav = new VerticalLayout(
                title,
                new RouterLink("Chart", ChartView.class),
                new RouterLink("Statistik", StatisticsView.class));
        nav.addClassName("main-nav");
        nav.setAlignItems(VerticalLayout.Alignment.CENTER);

        addToNavbar(nav);
    }
}
