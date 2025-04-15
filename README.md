Xây dựng và triển khai ứng dụng bán hàng dùng AWS EKS và GitOps
 ![image](https://github.com/user-attachments/assets/8df42dea-f705-4225-b103-d9ce9ce9958a)


Project Overview
Đồ án này trình bày cách triển khai web microservice bán hàng bằng một bộ công cụ và phương pháp DevOps. Các công cụ chính bao gồm:
•	Terraform: Công cụ Infrastructure as Code (IaC) để tạo cơ sở hạ tầng AWS như các phiên bản EC2 và cụm EKS.
•	GitHub: Quản lý mã nguồn.
•	Jenkins: Công cụ tự động hóa CI/CD.
•	SonarQube: Công cụ phân tích chất lượng mã và cổng chất lượng.
•	NPM: Công cụ xây dựng cho NodeJS.
•	Aqua Trivy: Công cụ quét lỗ hổng bảo mật.
•	Docker: Công cụ chứa để tạo hình ảnh.
•	AWS EKS: Nền tảng quản lý vùng chứa.
•	ArgoCD: Công cụ triển khai liên tục.
•	Prometheus & Grafana: Công cụ giám sát và cảnh báo.
Kiến trúc web microservice được sử dụng:
 ![image](https://github.com/user-attachments/assets/b047acbe-3231-4ab5-9396-bc77cad26c54)


Kiến trúc hệ thống
Quy trình triển khai bao gồm các bước chính sau:
1.	Quản lý mã nguồn
•	Developer thực hiện đẩy mã nguồn lên Git repository.
2.	Tích hợp liên tục (CI) – Jenkins
•	Jenkins đóng vai trò điều phối toàn bộ pipeline CI:
	Biên dịch và kiểm thử với Apache Maven.
	Phân tích mã nguồn tĩnh với SonarQube để đánh giá chất lượng mã và nợ kỹ thuật.
	Quét bảo mật:
o	Trivy FS scan: Quét mã nguồn và thư viện.
o	Trivy image scan: Quét lỗ hổng bảo mật trong Docker image.
	Cài đặt thư viện phụ thuộc thông qua NPM (nếu là ứng dụng Node.js).
	Đóng gói Docker: Xây dựng Docker image và đẩy lên Docker Registry.
	Quét bảo mật Docker Image: Trivy scan image. 	
3.	Thông báo trạng thái
•	Sau khi pipeline chạy xong, Jenkins gửi thông báo qua email cho developer (thành công hoặc thất bại).
4.	Triển khai liên tục (CD) theo GitOps
•	Jenkins cập nhật repository GitOps chứa cấu hình triển khai.
•	ArgoCD theo dõi GitOps repo, tự động đồng bộ trạng thái hệ thống với cụm Kubernetes trên AWS EKS.
5.	Giám sát và quan sát hệ thống
•	Hệ thống được giám sát bởi:
	Prometheus: Thu thập và lưu trữ metrics.
	Grafana: Trực quan hóa dữ liệu và hiển thị dashboard theo thời gian thực.

Pipeline Overview
Pipeline Stages CI Job
1.	Git Checkout: Sao chép mã nguồn từ GitHub.
2.	Build Application: Thực hiện biên dịch và đóng gói ứng dụng
3.	SonarQube Analysis: Thực hiện phân tích mã tĩnh.
4.	Quality Gate: Đảm bảo tiêu chuẩn chất lượng mã.
5.	Install NPM Dependencies: Cài đặt các gói NodeJS.
6.	Trivy Security Scan: Quét lỗ hổng bảo mật của dự án.
7.	Docker Build: Xây dựng hình ảnh Docker cho dự án.
8.	Push to Dockerhub: Gắn thẻ và đẩy image lên Docker lên Dockerhub.
9.	Image Cleanup: Xóa hình ảnh khỏi máy chủ Jenkins để tiết kiệm dung lượng.
Pipeline Stages CI Job
1.	Cleanup Workspace: Dọn dẹp không gian làm việc, xóa các tệp tin tạm.
2.	Checkout from Git: Kiểm tra mã nguồn từ GitHub.
3.	Update the Deployment Tags: Cập nhật các tag trong file deployment.yaml.
4.	Push the changed deployment file to GitHub: Đẩy file deployment.yaml đã thay đổi trở lại GitHub. Cấu hình thông tin người dùng Git, thực hiện git add, git commit, và sau đó đẩy thay đổi lên repository trên GitHub.
https://github.com/tien22521469/devops-gitops


