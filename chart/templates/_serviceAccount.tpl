
{{/*
Determine whether the serviceAccount should be created by examining local and global values
*/}}
{{- define "omar-stager.serviceAccount.enabled" -}}
{{- $globals := and (hasKey .Values.global.serviceAccount "enabled") (kindIs "bool" .Values.global.serviceAccount.enabled) -}}
{{- $locals := and (hasKey .Values.serviceAccount "enabled") (kindIs "bool" .Values.serviceAccount.enabled) -}}
{{- if $locals }}
{{-   .Values.serviceAccount.enabled }}
{{- else if $globals }}
{{-  .Values.global.serviceAccount.enabled }}
{{- else }}
{{-   true }}
{{- end -}}
{{- end -}}

{{/*
Determine the serviceAccount class name
*/}}
{{- define "omar-stager.serviceAccount.name" -}}
{{-   if eq (include "omar-stager.serviceAccount.enabled" $) "true" }}
{{-     pluck "name" .Values.serviceAccount .Values.global.serviceAccount | first | default $.Values.appName -}}
{{-   else }}
{{-     pluck "name" .Values.serviceAccount .Values.global.serviceAccount | first | default "" -}}
{{-   end }}
{{- end -}}
