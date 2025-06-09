<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
</head>
<body>
  <h1>Xây dựng và triển khai ứng dụng bán hàng dùng AWS EKS và GitOps</h1>
  <img src="emartapp/nodeapi/images/devops.png" style="max-width: 100%">

  <h2>Project Overview</h2>
  <p>Đồ án này trình bày cách triển khai web microservice bán hàng bằng một bộ công cụ và phương pháp DevOps. Các công cụ chính bao gồm:</p>
  <ul>
    <li>Terraform: Công cụ Infrastructure as Code (IaC) để tạo cơ sở hạ tầng AWS như các phiên bản EC2 và cụm EKS.</li>
    <li>GitHub: Quản lý mã nguồn.</li>
    <li>Jenkins: Công cụ tự động hóa CI/CD.</li>
    <li>SonarQube: Công cụ phân tích chất lượng mã và cổng chất lượng.</li>
    <li>NPM: Công cụ xây dựng cho NodeJS.</li>
    <li>Aqua Trivy: Công cụ quét lỗ hổng bảo mật.</li>
    <li>Docker: Công cụ chứa để tạo hình ảnh.</li>
    <li>AWS EKS: Nền tảng quản lý vùng chứa.</li>
   
    
  </ul>

  <h2>Kiến trúc hệ thống</h2>
  <img src="emartapp/nodeapi/images/micro.png" style="max-width: 100%"
  <p>Quy trình triển khai bao gồm các bước chính sau:</p>

  <h3>1. Quản lý mã nguồn</h3>
  <ul>
    <li>Developer thực hiện đẩy mã nguồn lên Git repository.</li>
  </ul>

  <h3>2. Tích hợp liên tục (CI) – Jenkins</h3>
  <p>Jenkins đóng vai trò điều phối toàn bộ pipeline CI:</p>
  <ul>
    <li>Biên dịch và kiểm thử với Apache Maven.</li>
    <li>Phân tích mã nguồn tĩnh với SonarQube để đánh giá chất lượng mã và nợ kỹ thuật.</li>
    <li>Quét bảo mật:
      <ul>
        <li>Trivy FS scan: Quét mã nguồn và thư viện.</li>
        <li>Trivy image scan: Quét lỗ hổng bảo mật trong Docker image.</li>
      </ul>
    </li>
    <li>Cài đặt thư viện phụ thuộc thông qua NPM (nếu là ứng dụng Node.js).</li>
    <li>Đóng gói Docker: Xây dựng Docker image và đẩy lên Docker Registry.</li>
    <li>Quét bảo mật Docker Image: Trivy scan image.</li>
  </ul>

  <h3>3. Thông báo trạng thái</h3>
  <ul>
    <li>Sau khi pipeline chạy xong, Jenkins gửi thông báo qua email cho developer (thành công hoặc thất bại).</li>
  </ul>

  <h3>4. Triển khai liên tục (CD) theo GitOps</h3>
  <ul>
    <li>Jenkins cập nhật repository GitOps chứa cấu hình triển khai.</li>
    <li>ArgoCD theo dõi GitOps repo, tự động đồng bộ trạng thái hệ thống với cụm Kubernetes trên AWS EKS.</li>
  </ul>

  <h3>5. Giám sát và quan sát hệ thống</h3>
  <ul>
    <li>Hệ thống được giám sát bởi:
      <ul>
        <li>Prometheus: Thu thập và lưu trữ metrics.</li>
        <li>Grafana: Trực quan hóa dữ liệu và hiển thị dashboard theo thời gian thực.</li>
      </ul>
    </li>
  </ul>

  <h2>Pipeline Overview</h2>

  <h3>Pipeline Stages - CI Job</h3>
  <ol>
    <li>Git Checkout: Sao chép mã nguồn từ GitHub.</li>
    <li>Build Application: Thực hiện biên dịch và đóng gói ứng dụng.</li>
    <li>SonarQube Analysis: Thực hiện phân tích mã tĩnh.</li>
    <li>Quality Gate: Đảm bảo tiêu chuẩn chất lượng mã.</li>
    <li>Install NPM Dependencies: Cài đặt các gói NodeJS.</li>
    <li>Trivy Security Scan: Quét lỗ hổng bảo mật của dự án.</li>
    <li>Docker Build: Xây dựng hình ảnh Docker cho dự án.</li>
    <li>Push to Dockerhub: Gắn thẻ và đẩy image lên Dockerhub.</li>
    <li>Image Cleanup: Xóa hình ảnh khỏi máy chủ Jenkins để tiết kiệm dung lượng.</li>
  </ol>

  <h3>Pipeline Stages - CD Job</h3>
  <ol>
    <li>Cleanup Workspace: Dọn dẹp không gian làm việc, xóa các tệp tin tạm.</li>
    <li>Checkout from Git: Kiểm tra mã nguồn từ GitHub.</li>
    <li>Update the Deployment Tags: Cập nhật các tag trong file deployment.yaml.</li>
    <li>Push the changed deployment file to GitHub: Đẩy file deployment.yaml đã thay đổi trở lại GitHub. Cấu hình thông tin người dùng Git, thực hiện git add, git commit, và sau đó đẩy thay đổi lên repository trên GitHub.</li>
  </ol>

  <p>Xem chi tiết mã nguồn tại GitHub: 
    <a href="https://github.com/tien22521469/devops-gitops" target="_blank">
      https://github.com/tien22521469/devops-gitops
    </a>
  </p>
</body>
</html>
