export type Role = "CANDIDATE" | "RECRUITER" | "ADMIN";

export type User = {
  id: string;
  fullName: string;
  email: string;
  role: Role;
  organizationId?: string | null;
};

export type AuthResponse = {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  user: User;
};

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type JobSkill = {
  id?: string;
  skillName: string;
  skillType?: string;
  weight?: number;
};

export type Job = {
  id: string;
  organizationId: string;
  createdBy?: string;
  title: string;
  description: string;
  location?: string;
  employmentType?: string;
  experienceMin?: number;
  experienceMax?: number;
  salaryMin?: number;
  salaryMax?: number;
  currency?: string;
  status: string;
  openings?: number | null;
  maxApplicants?: number | null;
  publishedAt?: string;
  createdAt?: string;
  skills?: JobSkill[];
};

export type ApplicationStatusHistory = {
  id: string;
  fromStatus?: string;
  toStatus: string;
  changedBy?: string;
  note?: string;
  changedAt: string;
};

export type Application = {
  id: string;
  jobId: string;
  organizationId: string;
  candidateProfileId: string;
  resumeId?: string;
  status: string;
  coverLetter?: string;
  appliedAt: string;
  updatedAt?: string;
  statusHistory?: ApplicationStatusHistory[];
};

export type CandidateProfile = {
  id: string;
  userId: string;
  headline?: string;
  summary?: string;
  experienceYears?: number;
  location?: string;
  linkedinUrl?: string;
  githubUrl?: string;
  phone?: string;
};

export type NotificationItem = {
  id: string;
  userId: string;
  type: string;
  title: string;
  message: string;
  link?: string;
  read: boolean;
  createdAt: string;
};

export type ScoreResult = {
  id: string;
  applicationId: string;
  jobId: string;
  fitScore: number;
  scoringMode: string;
  matchedSkills?: string[];
  missingSkills?: string[];
  explanation?: string;
};

export type InterviewQuestions = {
  id: string;
  applicationId: string;
  jobId: string;
  questions: string[];
  focusSkills?: string[];
  generatedBy?: string;
  createdAt?: string;
};
