export function StatusBadge({ status }: { status: string }) {
  const key = status.toUpperCase();
  let cls = "tp-badge tp-badge-neutral";
  if (["PUBLISHED", "SELECTED", "SHORTLISTED", "INTERVIEW"].includes(key)) {
    cls = "tp-badge tp-badge-ok";
  } else if (["DRAFT", "APPLIED", "SCREENING", "AI_SCORING", "RECRUITER_REVIEW"].includes(key)) {
    cls = "tp-badge tp-badge-warn";
  } else if (["CLOSED", "REJECTED"].includes(key)) {
    cls = "tp-badge tp-badge-danger";
  }

  return <span className={cls}>{status.replaceAll("_", " ")}</span>;
}
