package task

const (
	TypePing = "ping"
	TypeText = "text"
	TypeLink = "link"
)

type Task struct {
	Type string
	Text string
	Link string
}

func (t *Task) ToMap() map[string]string {
	return map[string]string{
		"Type": t.Type,
		"Text": t.Text,
		"Link": t.Link,
	}
}
