package task

const (
	TypePing = "ping" // no content or title
	TypeText = "text" // text as content, optional title
	TypeLink = "link" // text or link as content, optional title
)

type Task struct {
	Type  string
	Title string
	Text  string
	Link  string
}

func (t *Task) ToMap() map[string]string {
	return map[string]string{
		"Type":  t.Type,
		"Title": t.Title,
		"Text":  t.Text,
		"Link":  t.Link,
	}
}
