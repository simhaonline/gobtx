package com.gobtx.frontend.ws.news;

/** Created by Aaron Kuai on 2019/12/24. */
public class News implements Comparable<News> {

  protected long id;
  protected long oid;
  protected long sid; // 1 xiaocong 2  caijin
  protected boolean highlight;
  protected String title;
  protected String body;
  protected String urls;
  protected long time;

  public long getId() {
    return id;
  }

  public News setId(long id) {
    this.id = id;
    return this;
  }

  public long getOid() {
    return oid;
  }

  public News setOid(long oid) {
    this.oid = oid;
    return this;
  }

  public long getSid() {
    return sid;
  }

  public News setSid(long sid) {
    this.sid = sid;
    return this;
  }

  public boolean isHighlight() {
    return highlight;
  }

  public News setHighlight(boolean highlight) {
    this.highlight = highlight;
    return this;
  }

  public String getTitle() {
    return title;
  }

  public News setTitle(String title) {
    this.title = title;
    return this;
  }

  public String getBody() {
    return body;
  }

  public News setBody(String body) {
    this.body = body;
    return this;
  }

  public String getUrls() {
    return urls;
  }

  public News setUrls(String urls) {
    this.urls = urls;
    return this;
  }

  public long getTime() {
    return time;
  }

  public News setTime(long time) {
    this.time = time;
    return this;
  }

  @Override
  public String toString() {
    return "News{" + "id=" + id + ", title='" + title + '\'' + ", body='" + body + '\'' + '}';
  }

  @Override
  public int compareTo(News o) {
    // Desc
    long gap = this.getId() - o.getId();
    return gap > 0 ? -1 : (gap == 0 ? 0 : 1);
  }
}
