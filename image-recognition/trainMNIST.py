import torch
from torch.utils.data import DataLoader
from torchvision import transforms
from torchvision.datasets import MNIST
import matplotlib.pyplot as plt
from tqdm import tqdm


class Net(torch.nn.Module):
    def __init__(self):
        super().__init__()
        # 添加卷积层
        self.conv1 = torch.nn.Conv2d(1, 32, kernel_size=3)  # 输入为 1 个通道，输出为 32 个通道，卷积核大小为 3x3
        self.conv2 = torch.nn.Conv2d(32, 64, kernel_size=3)
        self.fc1 = torch.nn.Linear(64*24*24, 128)  # 适应新的特征数量
        self.fc2 = torch.nn.Linear(128, 10)

    def forward(self, x):
        x = torch.nn.functional.relu(self.conv1(x))  # 卷积层 1
        x = torch.nn.functional.relu(self.conv2(x))  # 卷积层 2
        x = x.view(-1, 64*24*24)  # 将特征展平
        x = torch.nn.functional.relu(self.fc1(x))   # 全连接层 1
        x = torch.nn.functional.log_softmax(self.fc2(x), dim=1)  # 全连接层 2，输出 log softmax
        return x


def get_data_loader(is_train):
    to_tensor = transforms.Compose([transforms.ToTensor()])
    data_set = MNIST("", is_train, transform=to_tensor, download=True)
    return DataLoader(data_set, batch_size=15, shuffle=True)


def evaluate(test_data, net):
    n_correct = 0
    n_total = 0
    with torch.no_grad():
        for (x, y) in tqdm(test_data, desc="Evaluating", leave=False):  # 用 tqdm 包裹测试数据集的迭代器
            outputs = net.forward(x)  # 不再展平，而是保持原始图像的形状
            for i, output in enumerate(outputs):
                if torch.argmax(output) == y[i]:
                    n_correct += 1
                n_total += 1
    return n_correct / n_total

def main():
    train_data = get_data_loader(is_train=True)
    test_data = get_data_loader(is_train=False)
    net = Net()

    print("Initial accuracy:", evaluate(test_data, net))  # 打印模型初始的准确率

    optimizer = torch.optim.Adam(net.parameters(), lr=0.001)  # 使用 Adam 优化器，学习率为 0.001

    for epoch in range(2):  # 进行 5 个训练轮次
        # 用 tqdm 包裹训练数据集的迭代器
        train_loader = tqdm(train_data, desc=f"Epoch {epoch+1}/{5}", unit="batch")
        for (x, y) in train_loader:  # 遍历训练数据集中的每个批次
            net.zero_grad()  # 清零模型参数的梯度
            output = net.forward(x)  # 输入形状为 [batch_size, 1, 28, 28]
            loss = torch.nn.functional.nll_loss(output, y)  # 计算负对数似然损失
            loss.backward()  # 反向传播计算梯度
            optimizer.step()  # 使用优化器更新模型参数

            # 在 tqdm 进度条上显示当前损失
            train_loader.set_postfix(loss=loss.item())

        # 每个 epoch 结束后在测试集上评估模型，并打印当前准确率
        print("Epoch", epoch + 1, "accuracy:", evaluate(test_data, net))

    misclassified = []
    with torch.no_grad():
        for (x, y) in test_data:
            outputs = net.forward(x)
            for i in range(len(outputs)):
                predicted_label = torch.argmax(outputs[i])
                if predicted_label != y[i]:
                    misclassified.append((x[i], y[i], predicted_label))
                if len(misclassified) >= 3:
                    break
            if len(misclassified) >= 3:
                break

    # Display the first three misclassified images
    for n, (img, true_label, predicted_label) in enumerate(misclassified):
        plt.figure(n)
        plt.imshow(img.squeeze(), cmap="gray")  # 修改展平方式，保持原始的 28x28 形状
        plt.title(f"True label: {int(true_label)}, Prediction: {int(predicted_label)}")
    plt.show()


if __name__ == "__main__":
    main()