package com.devbrief.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "briefings")
public class Briefing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private TopicCluster cluster;

    @Column(nullable = false, length = 1200)
    private String summary;

    @Column(nullable = false, length = 1200)
    private String whyItMatters;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "briefing_key_points", joinColumns = @JoinColumn(name = "briefing_id"))
    @Column(name = "point", length = 500)
    private List<String> keyPoints = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "briefing_action_items", joinColumns = @JoinColumn(name = "briefing_id"))
    @Column(name = "action_item", length = 500)
    private List<String> actionItems = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "briefing_risk_notes", joinColumns = @JoinColumn(name = "briefing_id"))
    @Column(name = "risk_note", length = 500)
    private List<String> riskNotes = new ArrayList<>();

    @Column(nullable = false)
    private int readingMinutes;

    @Column(nullable = false)
    private Instant generatedAt;

    protected Briefing() {
    }

    private Briefing(TopicCluster cluster, String summary, String whyItMatters, List<String> keyPoints,
                     List<String> actionItems, List<String> riskNotes, int readingMinutes) {
        this.cluster = cluster;
        this.summary = summary;
        this.whyItMatters = whyItMatters;
        this.keyPoints = new ArrayList<>(keyPoints);
        this.actionItems = new ArrayList<>(actionItems);
        this.riskNotes = new ArrayList<>(riskNotes);
        this.readingMinutes = readingMinutes;
        this.generatedAt = Instant.now();
    }

    public static Briefing create(TopicCluster cluster, String summary, String whyItMatters, List<String> keyPoints,
                                  List<String> actionItems, List<String> riskNotes, int readingMinutes) {
        return new Briefing(cluster, summary, whyItMatters, keyPoints, actionItems, riskNotes, readingMinutes);
    }

    public Long getId() {
        return id;
    }

    public TopicCluster getCluster() {
        return cluster;
    }

    public String getSummary() {
        return summary;
    }

    public String getWhyItMatters() {
        return whyItMatters;
    }

    public List<String> getKeyPoints() {
        return List.copyOf(keyPoints);
    }

    public List<String> getActionItems() {
        return List.copyOf(actionItems);
    }

    public List<String> getRiskNotes() {
        return List.copyOf(riskNotes);
    }

    public int getReadingMinutes() {
        return readingMinutes;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }
}

