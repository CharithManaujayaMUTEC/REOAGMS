# Kubernetes starter manifests

These manifests provide a single-replica development starter for the services, individual PostgreSQL StatefulSets/PVCs, RabbitMQ, and the React/Vite development server. They are not a production deployment.

Before applying:

1. Build and publish all images using the names referenced in `apps.yml` (`reoagms/<service>:latest` and `reoagms/frontend:dev`), or edit image names to your registry. The frontend image should be built with `frontend/Dockerfile.dev`.
2. Copy `secret.example.yml` to `secret.yml`, replace every development credential with proper secrets, and add `secret.yml` to `kustomization.yaml`. Do not commit real secrets.
3. Confirm your cluster supports persistent volumes and has enough CPU/RAM.
4. Configure an Ingress with TLS for an externally reachable environment.

Apply after preparing images and secrets:

```bash
kubectl apply -k k8s/base
kubectl -n reoagms get pods,svc,pvc
```

The manifests do not yet include production ingress/TLS, backups, autoscaling, PodDisruptionBudgets, network policies, managed secret integration, or production-grade database/RabbitMQ clustering. Use Docker Compose as the supported local demonstration environment.
